package io.github.thisdk.camera.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import frpclib.Frpclib
import java.io.File

@AndroidEntryPoint
class FRpcLibService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Frpclib.run(File(cacheDir, "temp_frpc.ini").absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return super.onStartCommand(intent, flags, startId)
    }


}