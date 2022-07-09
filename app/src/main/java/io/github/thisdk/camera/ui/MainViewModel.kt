package io.github.thisdk.camera.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.thisdk.camera.component.SingleLiveEvents
import io.github.thisdk.camera.data.MJpgStreamService
import io.github.thisdk.camera.ktx.asLiveData
import io.github.thisdk.camera.ktx.setState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val service: MJpgStreamService
) : ViewModel() {

    private val _viewStates = MutableLiveData(MainViewAES.MainViewState(log = "", enable = true))
    val viewStates = _viewStates.asLiveData()

    private val _viewEvents: SingleLiveEvents<MainViewAES.MainViewEvent> = SingleLiveEvents()
    val viewEvents = _viewEvents.asLiveData()

    fun dispatch(action: MainViewAES.MainViewAction) {
        when (action) {
            is MainViewAES.MainViewAction.FetchStream -> fetchStream()
        }
    }

    private fun fetchStream() {
        viewModelScope.launch {
            flow<InputStream?> {
                showLog("信息 : 开始请求视频流...")
                emit(service.stream().byteStream())
            }.flowOn(Dispatchers.IO)
                .retryWhen { cause, attempt ->
                    if (attempt > 9) {
                        showLog("错误 : 重试超过10次,请检查网络,或者再次重试!")
                        return@retryWhen false
                    }
                    if (cause is ConnectException) {
                        showLog("错误 : 异常 ${cause.message}")
                        return@retryWhen false
                    }
                    showLog("错误 : 异常 ${cause.message}")
                    showLog("信息 : 即将开始第 ${attempt + 1} 次重试")
                    delay(1000)
                    cause is Exception || cause is java.lang.Exception
                }
                .catch { e ->
                    if (e is ConnectException) {
                        showLog("信息 : FRpc服务异常,即将重启!")
                        _viewEvents.setEvent(
                            MainViewAES.MainViewEvent.FRpcLibConnError
                        )
                        return@catch
                    }
                    emit(null)
                }
                .collect {
                    if (it != null) {
                        showLog("信息 : 内网穿透成功!")
                    } else {
                        _viewStates.setState {
                            copy(enable = true)
                        }
                    }
                    _viewStates.setState {
                        copy(stream = it)
                    }
                }
        }

    }

    private fun showLog(log: String) {
        viewModelScope.launch(Dispatchers.Main) {
            var displayLog = viewStates.value?.log!!
            val split = displayLog.split("\n")
            if (split.size > 17) {
                displayLog = displayLog.replaceRange(0, split[0].length + 1, "")
            }
            displayLog += "$log\n"
            _viewStates.setState {
                copy(log = displayLog)
            }
        }
    }


}
