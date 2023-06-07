package ru.vagavagus.customwebview

import java.lang.Exception

interface OkHttpClientCallback {
    fun onSuccess(data: ResponseJson?)
    fun onError(exception: Exception)
}