package com.webvirt;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * WebViewClient personalizado para WebVirt con control de seguridad externo.
 */
public class WebVirtClient extends WebViewClient {
    private static final String TAG = "WebVirtClient";
    private final WebVirt webVirt;

    public WebVirtClient(@NonNull WebVirt webVirt) {
        this.webVirt = webVirt;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, 
                                                      @NonNull WebResourceRequest request) {
        String url = request.getUrl().toString();

        // 1. Peticiones al host virtual → servir desde assets
        if (webVirt.shouldIntercept(url)) {
            return webVirt.serveAsset(url);
        }

        // 2. Peticiones externas (API, CDN, etc.)
        if (webVirt.shouldAllowExternalRequest(url)) {
            Log.d(TAG, "🌐 Allowing external request: " + url);
            return super.shouldInterceptRequest(view, request); // dejar que WebView maneje normalmente
        }

        // 3. Bloquear todo lo demás (seguridad por defecto)
        Log.w(TAG, "⛔ Blocked external request: " + url);
        return createBlockedResponse();
    }

    private WebResourceResponse createBlockedResponse() {
        String html = "<!DOCTYPE html><html><head><meta charset='utf-8'>" +
                "<title>Blocked</title></head>" +
                "<body style='font-family:sans-serif;padding:2rem;text-align:center'>" +
                "<h1>🚫 Access Blocked</h1>" +
                "<p>This external resource has been blocked by WebVirt for security.</p>" +
                "<small>WebVirt Security</small></body></html>";

        return new WebResourceResponse("text/html", "UTF-8", 403, "Forbidden",
                new HashMap<>(), new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
    }

    // onPageFinished, onReceivedError, onReceivedHttpError se mantienen igual
    @Override
    public void onPageFinished(@NonNull WebView view, @NonNull String url) {
        super.onPageFinished(view, url);
        Log.d(TAG, "✅ Page loaded: " + url);
    }

    @Override
    public void onReceivedError(@NonNull WebView view, int errorCode, 
                                @NonNull String description, @NonNull String failingUrl) {
        Log.e(TAG, "❌ Error " + errorCode + ": " + description + " URL: " + failingUrl);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}