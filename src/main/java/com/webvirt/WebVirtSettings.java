package com.webvirt;

import android.annotation.SuppressLint;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Configurador de WebViewSettings para WebVirt.
 * Aplica settings óptimos para SPAs modernas.
 */
public class WebVirtSettings {
    
    @SuppressLint("SetJavaScriptEnabled")
    public static void apply(WebView webView, WebVirtConfig config) {
        WebSettings settings = webView.getSettings();
        
        // JavaScript y almacenamiento
        settings.setJavaScriptEnabled(config.isJavascriptEnabled());
        settings.setDomStorageEnabled(config.isDomStorageEnabled());
        settings.setDatabaseEnabled(true);
        
        // Caché
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Viewport
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setTextZoom(100);
        
        // Seguridad
        settings.setMixedContentMode(config.getMixedContentMode());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }
        
        // Zoom
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        
        // Cookies
        configureCookies(webView);
    }
    
    private static void configureCookies(WebView webView) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);
    }
}