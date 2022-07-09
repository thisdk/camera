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

        runBlocking(Dispatchers.Default) {
            val file = File(cacheDir, "frpc.ini")
            if (!file.exists()) {
                assets.open("frpc.ini").bufferedReader().use {
                    file.writeText(it.readText())
                }
            }
        }

    }

}