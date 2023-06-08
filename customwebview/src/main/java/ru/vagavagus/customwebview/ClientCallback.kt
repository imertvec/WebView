package ru.vagavagus.customwebview

import java.lang.Exception

interface ClientCallback {
    fun onSuccess(data: ResponseJson?)
    fun onError(exception: Exception)
    fun onBackPressed()
}