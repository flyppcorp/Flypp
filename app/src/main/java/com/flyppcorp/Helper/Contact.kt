package com.flyppcorp.Helper

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.flyppcorp.flypp.R
import kotlinx.android.synthetic.main.activity_contact.*

class Contact : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("http://flyppbrasil.epizy.com/parceiros.html")
        val webSetting = webView.settings
        webSetting.javaScriptEnabled = true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack()
        }else{
            super.onBackPressed()
        }

    }
}