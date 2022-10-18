package io.github.thisdk.camera.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.thisdk.camera.component.SingleLiveEvents
import io.github.thisdk.camera.data.MJpgStreamService
import io.github.thisdk.camera.ktx.asLiveData
import io.github.thisdk.camera.ktx.setState
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.net.ConnectException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val service: MJpgStreamService
) : ViewModel() {

    private var streamDeferred: Deferred<Unit>? = null

    private val _viewStates = MutableLiveData(MainViewAES.MainViewState(log = "", enable = true))
    val viewStates = _viewStates.asLiveData()

    private val _viewEvents: SingleLiveEvents<MainViewAES.MainViewEvent> = SingleLiveEvents()
    val viewEvents = _viewEvents.asLiveData()

    fun dispatch(action: MainViewAES.MainViewAction) {
        when (action) {
            is MainViewAES.MainViewAction.FetchStream -> fetchStream()
            is MainViewAES.MainViewAction.ButtonEnable -> setButtonEnable(action.enable)
            is MainViewAES.MainViewAction.ShowLog -> showLog(action.log)
        }
    }

    private fun fetchStream() {
        if (streamDeferred != null && streamDeferred?.isActive!!) {
            showLog("信息 : 取消已有协程任务")
            streamDeferred?.cancel()
        }
        streamDeferred = viewModelScope.async {
            setButtonEnable(false)
            flow {
                emit(service.stream().byteStream())
            }.onStart {
                showLog("信息 : 开始穿透内网,请求视频流...")
            }.retryWhen { cause, attempt ->
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
                delay(500)
                cause is Exception
            }.catch { e ->
                if (e is ConnectException) {
                    showLog("错误 : FRpc服务异常,即将重启!")
                    _viewEvents.setEvent(
                        MainViewAES.MainViewEvent.FRpcLibConnError
                    )
                    return@catch
                }
                setButtonEnable(true)
            }.onEach {
                showLog("信息 : 内网穿透成功,读取视频流...")
                _viewStates.setState {
                    copy(stream = it)
                }
            }.collect()
        }
    }

    private fun setButtonEnable(enable: Boolean) {
        _viewStates.setState {
            copy(enable = enable)
        }
    }

    private fun showLog(log: String) {
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
