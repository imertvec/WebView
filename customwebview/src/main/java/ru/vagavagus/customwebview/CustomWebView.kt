package ru.vagavagus.customwebview

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URLDecoder


@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class CustomWebView (
    private val context: Context,
    private val clientCallback: ClientCallback
): WebView(context), Callback {
    private val okHttpClient = OkHttpClient()

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = true
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            setSupportZoom(false)
            webViewClient = CustomWebViewClient()
            webChromeClient = CustomWebChromeClient()
        }

        isFocusableInTouchMode = true
        focusable = View.FOCUSABLE
    }

    fun doPost(url: String, deviceInfo: DeviceInfo) {
        val formBody = FormBody.Builder()
            .add("phone_name", deviceInfo.phoneName)
            .add("locale", deviceInfo.locale)
            .add("unique", deviceInfo.unique)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        okHttpClient.newCall(request).enqueue(this)
    }

    private inner class CustomWebViewClient: WebViewClient() {
        private val tag = this::class.simpleName
        private val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(context))
            .build()

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return try {
                val url = URLDecoder.decode(request?.url.toString(), "UTF-8")
                Log.i(tag, "?url= $url");
                view?.loadUrl(url)
                true
            } catch (e: Exception) {
                super.shouldOverrideUrlLoading(view, request)
            }
        }

        //MARK: Save user data implementation
        override fun onPageFinished(view: WebView?, url: String?) {
            CookieManager.getInstance().flush()
        }
    }

    private inner class CustomWebChromeClient: WebChromeClient() {
        private val tag = this::class.simpleName
        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null
        private var originalOrientation = 0
        private var originalSystemVisibility = 0
        private val activityContext = context as ComponentActivity

        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            return try {
                val transport = resultMsg!!.obj as WebViewTransport
                transport.webView = view
                resultMsg.sendToTarget()
                true
            } catch (e: Exception) {
                Log.e(tag, "onCreateWindow: ", e)
                false
            }
        }

        override fun onShowFileChooser(
            mWebView: WebView?,
            filePathCallback: ValueCallback<Array<Uri?>?>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            return try {
                clientCallback.onShowFileChooser(fileChooserParams, filePathCallback)
                true
            } catch (e: ActivityNotFoundException) {
                clientCallback.onShowFileChooser(fileChooserParams, null)
                false
            }
        }

        //MARK: fullscreen implementation
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (customView == null) null
            else BitmapFactory.decodeResource(context.resources, 2130837573)
        }

        override fun onHideCustomView() = with(activityContext){
            (window.decorView as FrameLayout).removeView(customView)
            customView = null
            window.decorView.systemUiVisibility = originalSystemVisibility
            requestedOrientation = originalOrientation
            customViewCallback?.onCustomViewHidden()
            customViewCallback = null
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            if(this.customView != null) {
                onHideCustomView()
                return
            }

            customView = view
            originalSystemVisibility = activityContext.window.decorView.systemUiVisibility
            originalOrientation = activityContext.requestedOrientation
            customViewCallback = callback
            (activityContext.window.decorView as FrameLayout).addView(
                customView,
                FrameLayout.LayoutParams(-1, -1)
            )

            activityContext.window.decorView.systemUiVisibility = 3846
        }
    }

    //MARK: Callback implementation
    override fun onFailure(call: Call, e: IOException) {
        clientCallback.onError(e)
    }

    override fun onResponse(call: Call, response: Response) {
        val gson = Gson().fromJson(response.body?.string(), ResponseJson::class.java)
        clientCallback.onSuccess(gson)
    }
}