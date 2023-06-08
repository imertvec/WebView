package ru.vagavagus.customwebview

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onResponseLink: (link: String) -> Unit = { },
    onResponseNo: () -> Unit,
    onResponseNoPush: () -> Unit,
    onError: (err: String) -> Unit = { onResponseNo() },
    circularColor: Color = Color.Black
) {
    var responseType: String? by remember { mutableStateOf(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var webView: CustomWebView? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = webView) {
        webView?.doPost(url = url, deviceInfo = deviceInfo)
    }

    BackHandler {
        webView?.goBack()
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                webView = CustomWebView(
                    context,
                    clientCallback = object : ClientCallback {
                        override fun onSuccess(data: ResponseJson?) {
                            responseType = data?.url
                        }
                        override fun onError(exception: Exception) {
                            responseType = exception.message
                        }
                    }
                )
                webView!!
            }
        )

        if(webView != null) {
            if(responseType != null) {
                when(responseType) {
                    ResponseType.NoPush.name -> {
                        webView = null
                        onResponseNoPush()
                    }
                    ResponseType.No.name -> {
                        webView = null
                        onResponseNo()
                    }
                    else -> {
                        if(responseType!!.contains("http")) {
                            webView!!.loadUrl(responseType!!)
                            onResponseLink(responseType!!)
                        } else {
                            error = responseType!!
                            onError(responseType!!)
                        }
                    }
                }

                loading = false
            }
        }


        if(loading)
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = circularColor
            )

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