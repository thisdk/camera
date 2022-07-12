package io.github.thisdk.camera.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.thisdk.camera.databinding.FragmentMainBinding
import io.github.thisdk.camera.ktx.observeEvent
import io.github.thisdk.camera.ktx.observeState
import io.github.thisdk.camera.ktx.toast
import io.github.thisdk.camera.mjpeg.DisplayMode
import io.github.thisdk.camera.mjpeg.MjpegInputStreamDefault
import io.github.thisdk.camera.mjpeg.OnPlayerStreamStateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var first = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.start.setOnClickListener {
            viewModel.dispatch(MainViewAES.MainViewAction.FetchStream)
        }
        binding.video.setDisplayMode(DisplayMode.BEST_FIT)
        binding.video.showFps(false)
        binding.video.setPlayerStreamStateCallback(object : OnPlayerStreamStateCallback {
            override fun streamError() {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.dispatch(MainViewAES.MainViewAction.ButtonEnable(true))
                    viewModel.dispatch(MainViewAES.MainViewAction.ShowLog("错误 : 视频流异常..."))
                    binding.video.clearStream()
                    binding.start.text = "视频流异常,点击重试!"
                }
            }
        })
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.viewStates.let { state ->
            state.observeState(viewLifecycleOwner, MainViewAES.MainViewState::enable) {
                binding.start.isEnabled = it
            }
            state.observeState(viewLifecycleOwner, MainViewAES.MainViewState::log) {
                binding.log.text = it
            }
            state.observeState(viewLifecycleOwner, MainViewAES.MainViewState::stream) {
                if (it == null) return@observeState
                viewModel.dispatch(MainViewAES.MainViewAction.ButtonEnable(false))
                binding.video.setSource(MjpegInputStreamDefault(it))
                binding.start.text = "内网穿透成功"
            }
        }
        viewModel.viewEvents.observeEvent(viewLifecycleOwner) {
            when (it) {
                is MainViewAES.MainViewEvent.ShowToastStr -> context?.toast(it.message)
                is MainViewAES.MainViewEvent.ShowToastRes -> context?.toast(it.message)
                is MainViewAES.MainViewEvent.FRpcLibConnError -> {
                    if (!binding.video.isStreaming) {
                        (activity as MainActivity).stopFRpcLibService()
                        (activity as MainActivity).startFRpcLibService()
                        lifecycleScope.launch {
                            binding.start.text = "正在重启FRpc服务..."
                            viewModel.dispatch(MainViewAES.MainViewAction.ShowLog("信息 : 正在重启FRpc服务..."))
                            viewModel.dispatch(MainViewAES.MainViewAction.ButtonEnable(false))
                            delay(8000)
                            viewModel.dispatch(MainViewAES.MainViewAction.ButtonEnable(true))
                            viewModel.dispatch(MainViewAES.MainViewAction.ShowLog("信息 : 请尝试重新连接"))
                            binding.start.text = "尝试重新连接"
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!first) {
            binding.start.text = "正在重新获取流..."
            binding.start.callOnClick()
        }
        first = false
    }

    override fun onPause() {
        super.onPause()
        binding.video.stopPlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}