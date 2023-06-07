package ru.vagavagus.customwebview

import com.google.gson.annotations.SerializedName

data class ResponseJson(
    @SerializedName("url") val url: String
)