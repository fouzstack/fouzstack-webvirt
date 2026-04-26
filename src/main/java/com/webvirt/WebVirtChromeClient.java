package com.webvirt;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * WebChromeClient minimalista para WebVirt.
 */
public class WebVirtChromeClient extends WebChromeClient {
    
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
    }
}