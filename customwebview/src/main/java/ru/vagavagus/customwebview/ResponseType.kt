package ru.vagavagus.customwebview

sealed class ResponseType(val name: String) {
    object No: ResponseType("no")
    object NoPush: ResponseType("nopush")
}
