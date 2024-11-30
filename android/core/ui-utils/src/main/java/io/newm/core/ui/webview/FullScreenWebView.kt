package io.newm.core.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FullScreenWebView(context: Context, url: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val currentUrl = request?.url.toString()
                        return if (currentUrl.startsWith("https://newm.io/")) {
                            // Load the URL in the current WebView
                            false
                        } else {
                            // Open URLs that don't start with "https://newm.io/" in an external browser or another WebView
                            launchExternalUrl(context, currentUrl)
                            true
                        }
                    }
                }

                settings.javaScriptEnabled = true

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun launchExternalUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}