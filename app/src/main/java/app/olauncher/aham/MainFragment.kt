package app.olauncher.aham

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment(
    private val url: String,
    private val webChromeClient: WebChromeClient,
    private val webViewClient: WebViewClient
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        loadUrl()
    }

    fun loadUrl() = theWebView.loadUrl(url)

    fun loadUrl(customUrl: String) = theWebView.loadUrl(customUrl)

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        theWebView.settings.javaScriptEnabled = true
        theWebView.webChromeClient = webChromeClient
        theWebView.webViewClient = webViewClient
    }
}