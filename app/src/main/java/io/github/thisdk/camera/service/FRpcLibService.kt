package io.github.thisdk.camera.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import frpclib.Frpclib
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File

@AndroidEntryPoint
class FRpcLibService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val config = File(cacheDir, "frpc.ini")
        if (config.exists() && config.canRead()) {
            startFRpcLibService(config)
        } else {
            runBlocking {
                delay(2000)
                startFRpcLibService(config)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startFRpcLibService(config: File) {
        try {
            Frpclib.run(config.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}