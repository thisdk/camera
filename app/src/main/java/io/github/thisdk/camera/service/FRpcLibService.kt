package io.github.thisdk.camera.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import frpclib.Frpclib
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.concurrent.thread

@AndroidEntryPoint
class FRpcLibService : Service() {

    override fun onCreate() {
        super.onCreate()
        val config = File(cacheDir, "frpc.ini")
        if (config.exists() && config.canRead()) {
            startFRpcLibService(config)
        } else {
            runBlocking {
                delay(2000)
                startFRpcLibService(config)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    private fun startFRpcLibService(config: File) {
        thread {
            try {
                Frpclib.run(config.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}