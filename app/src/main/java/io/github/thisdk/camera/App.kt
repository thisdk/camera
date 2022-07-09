package io.github.thisdk.camera

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        runBlocking(Dispatchers.IO) {
            val file = File(cacheDir, "temp_frpc.ini")
            if (!file.exists()) {
                assets.open("frpc.ini").bufferedReader().use {
                    file.writeText(it.readText())
                }
            }
        }

    }

}