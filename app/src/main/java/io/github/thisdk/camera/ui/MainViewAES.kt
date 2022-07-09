package io.github.thisdk.camera.ui

import androidx.annotation.StringRes
import java.io.InputStream

class MainViewAES {

    sealed class MainViewAction {
        object FetchStream : MainViewAction()
    }

    sealed class MainViewEvent {
        data class ShowToastStr(val message: String) : MainViewEvent()
        data class ShowToastRes(@StringRes val message: Int) : MainViewEvent()
        object FRpcLibConnError : MainViewEvent()
    }

    data class MainViewState(
        var stream: InputStream? = null,
        val log: String,
        val enable: Boolean
    )

}