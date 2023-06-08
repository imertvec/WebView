package ru.vagavagus.customwebview

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebView(
    modifier: Modifier = Modifier,
    deviceInfo: DeviceInfo,
    url: String,
    onResponseLink: (link: String) -> Unit,
    onResponseNo: () -> Unit,
    onResponseNoPush: () -> Unit,
    onError: (err: String) -> Unit = { onResponseNo() }
) {
    var responseType: String? by remember { mutableStateOf(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var view: CustomWebView? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                view = CustomWebView(
                    context,
                    clientCallback = object : ClientCallback {
                        override fun onSuccess(data: ResponseJson?) {
                            responseType = data?.url
                        }

                        override fun onError(exception: Exception) {
                            responseType = exception.message
                        }

                        override fun onBackPressed() {
                            view!!.goBack()
                        }
                    }).also {
                    it.doPost(url = url, deviceInfo = deviceInfo)
                }

                view!!
            }
        )

        if(responseType != null) {
            when(responseType) {
                ResponseType.NoPush.name -> onResponseNoPush()
                ResponseType.No.name -> onResponseNo()
                else -> {
                    if(responseType!!.contains("http")) {
                        view?.loadUrl(responseType!!)
                        onResponseLink(responseType!!)
                    } else {
                        error = responseType!!
                        onError(responseType!!)
                    }
                }
            }

            loading = false
        }

        if(loading)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

        if(error.isNotEmpty() && responseType != null)
            Text(
                text = responseType!!,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
    }
}