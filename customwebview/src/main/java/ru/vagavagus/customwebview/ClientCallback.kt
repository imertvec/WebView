package ru.vagavagus.customwebview

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import java.lang.Exception

interface ClientCallback {
    fun onSuccess(data: ResponseJson?)
    fun onError(exception: Exception)
    fun onShowFileChooser(
        fileChooserParams: FileChooserParams,
        filePathCallback: ValueCallback<Array<Uri?>?>?
    )
}