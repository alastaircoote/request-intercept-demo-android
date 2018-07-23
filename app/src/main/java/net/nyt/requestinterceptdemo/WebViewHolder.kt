package net.nyt.requestinterceptdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

data class CacheEntry(
        val url:String,
        val statusCode:Int,
        val headers: Map<String,String>,
        val content:ByteArray
)

val cachedItems = arrayOf(
        CacheEntry(
                "https://request-intercept-demo.glitch.me/style.css",
                200,
                mapOf("Content-Type" to "text/css; charset=utf-8"),
                """
            body {
                background: red;
                color: white;
            }
            """.toByteArray(Charsets.UTF_8)
        )
)

// The reason phrase and status code have 1:1 relationship so not really sure why
// we need to specify it. But we can just hard-code, like so
val httpCodeReasons = mapOf(200 to "OK", 404 to "Not Found")

class CacheWebViewClient : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {

        val uri = request!!.url

        val cached = cachedItems.find { item -> item.url == uri.toString()} ?: return null

        return WebResourceResponse(
                cached.headers["Content-Type"],
                cached.headers["Content-Encoding"] ?: "",
                cached.statusCode,
                httpCodeReasons[cached.statusCode],
                // Android already sends the content-type and content-encoding headers
                // in the mimeType argument above, so let's not duplicate it here
                cached.headers.filter { header -> header.key != "Content-Type" && header.key != "Content-Encoding"},
                cached.content.inputStream()
        )

    }
}

class WebViewHolder : AppCompatActivity() {

    private var client = CacheWebViewClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this.baseContext)
        WebView.setWebContentsDebuggingEnabled(true)
        webView.webViewClient = this.client
        this.setContentView(webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.loadUrl("https://request-intercept-demo.glitch.me")

    }
}
