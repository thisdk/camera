package io.github.thisdk.camera

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import frpclib.Frpclib
import java.io.File

class FrpcService : Service() {

    override fun onCreate() {
        super.onCreate()
        Thread {
            kotlin.run {
                try {
                    Frpclib.run(File(filesDir, "frpc.ini").absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopSelf()
                }
            }
        }.start()
    }


    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }


}