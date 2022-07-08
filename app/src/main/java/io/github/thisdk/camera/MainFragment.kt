package io.github.thisdk.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.niqdev.mjpeg.DisplayMode
import com.github.niqdev.mjpeg.Mjpeg
import com.github.niqdev.mjpeg.MjpegInputStream
import io.github.thisdk.camera.databinding.FragmentMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

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
            playCameraStream()
        }
        lifecycleScope.launch {
            delay(3000)
            binding.start.isEnabled = true
        }
    }

    private fun playCameraStream() {
        binding.start.isEnabled = false
        Mjpeg.newInstance()
            .open("http://127.0.0.1:53866/?action=stream", 15)
            .subscribe(
                { stream: MjpegInputStream ->
                    binding.video.setSource(stream)
                    binding.video.setDisplayMode(DisplayMode.BEST_FIT)
                    binding.video.showFps(false)
                    binding.start.isEnabled = true
                    Toast.makeText(context, "内网穿透成功!", Toast.LENGTH_SHORT).show()
                }
            ) { throwable: Throwable? ->
                Toast.makeText(context, throwable?.message, Toast.LENGTH_SHORT).show()
                binding.start.isEnabled = true
            }
    }

    override fun onResume() {
        super.onResume()
        if (!first) playCameraStream()
        first = true
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