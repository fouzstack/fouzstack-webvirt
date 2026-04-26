package com.webvirt;

import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WebVirt {
	private static final String TAG = "WebVirt";
	
	private final WebVirtConfig config;
	private WebView webView;
	
	private final Map<String, byte[]> memoryCache = new HashMap<>();
	private volatile byte[] indexHtmlCache;
	
	private static final Map<String, String> MIME_TYPES = new HashMap<>();
	static {
		MIME_TYPES.put("html", "text/html");
		MIME_TYPES.put("htm", "text/html");
		MIME_TYPES.put("css", "text/css");
		MIME_TYPES.put("js", "application/javascript");
		MIME_TYPES.put("mjs", "application/javascript");
		MIME_TYPES.put("json", "application/json");
		MIME_TYPES.put("map", "application/json");
		MIME_TYPES.put("png", "image/png");
		MIME_TYPES.put("jpg", "image/jpeg");
		MIME_TYPES.put("jpeg", "image/jpeg");
		MIME_TYPES.put("gif", "image/gif");
		MIME_TYPES.put("svg", "image/svg+xml");
		MIME_TYPES.put("webp", "image/webp");
		MIME_TYPES.put("ico", "image/x-icon");
		MIME_TYPES.put("woff", "font/woff");
		MIME_TYPES.put("woff2", "font/woff2");
		MIME_TYPES.put("ttf", "font/ttf");
		MIME_TYPES.put("otf", "font/otf");
		MIME_TYPES.put("wasm", "application/wasm");
		MIME_TYPES.put("txt", "text/plain");
		MIME_TYPES.put("xml", "application/xml");
		MIME_TYPES.put("mp4", "video/mp4");
		MIME_TYPES.put("webm", "video/webm");
	}
	
	private WebVirt(Context context) {
		this.config = new WebVirtConfig();
		this.config.setContext(context.getApplicationContext());
	}
	
	@NonNull
	public static WebVirt with(@NonNull Context context) {
		return new WebVirt(context);
	}
	
	public WebVirt host(@NonNull String host) {
		config.setVirtualHost(host);
		return this;
	}
	
	public WebVirt subfolder(@NonNull String subfolder) {
		config.setAssetSubfolder(subfolder);
		return this;
	}
	
	public WebVirt allowExternalDomains(@NonNull String... domains) {
		for (String domain : domains) {
			config.addAllowedExternalDomain(domain);
		}
		return this;
	}
	
	public WebVirt offlineOnly(boolean enabled) {
		config.setOfflineOnly(enabled);
		return this;
	}
	
	public WebVirt config(@NonNull ConfigCallback callback) {
		callback.onConfigure(config);
		return this;
	}
	
	@NonNull
	public WebVirt bind(@NonNull WebView webView) {
		if (config.getVirtualHost() == null || config.getVirtualHost().isEmpty()) {
			throw new IllegalStateException("Host is required. Call host(\"miapp.local\") before bind()");
		}
		
		this.webView = webView;
		
		WebVirtSettings.apply(webView, config);
		webView.setWebViewClient(new WebVirtClient(this));
		webView.setWebChromeClient(new WebVirtChromeClient());
		
		Log.d(TAG, "✅ WebVirt bound to " + config.getVirtualHost());
		if (config.isOfflineOnly())
		Log.d(TAG, "🔒 Offline-only mode enabled");
		if (!config.getAllowedExternalDomains().isEmpty()) {
			Log.d(TAG, "🌐 Allowed external domains: " + config.getAllowedExternalDomains());
		}
		return this;
	}
	
	public static void quick(@NonNull Context context, @NonNull WebView webView, @NonNull String host) {
		with(context).host(host).bind(webView);
	}
	
	public String getBaseUrl() {
		return config.getBaseUrl();
	}
	
	public boolean shouldIntercept(@Nullable String url) {
		return url != null && (url.startsWith("https://" + config.getVirtualHost())
		|| url.startsWith("http://" + config.getVirtualHost()));
	}
	
	public boolean shouldAllowExternalRequest(@NonNull String url) {
		if (config.isOfflineOnly()) {
			return false;
		}
		
		if (config.getAllowedExternalDomains().isEmpty()) {
			return false;
		}
		
		try {
			String host = new URL(url).getHost();
			return config.isDomainAllowed(host);
			} catch (Exception e) {
			return false;
		}
	}
	
	@Nullable
	public WebResourceResponse serveAsset(@NonNull String url) {
		try {
			String relativePath = extractPath(url);
			String fullPath;
			
			if (relativePath.isEmpty() || isSpaRoute(relativePath)) {
				fullPath = config.getIndexAssetPath();
				} else {
				fullPath = config.getAssetSubfolder() + relativePath;
			}
			
			validatePath(fullPath);
			Log.d(TAG, "📄 Serving: " + fullPath);
			
			byte[] data = loadFromCache(fullPath);
			return buildResponse(fullPath, data);
			
			} catch (IOException e) {
			Log.e(TAG, "❌ Asset not found: " + e.getMessage());
			return errorResponse(404, "Asset not found: " + e.getMessage());
			} catch (SecurityException e) {
			Log.e(TAG, "🔒 Security: " + e.getMessage());
			return errorResponse(403, "Forbidden");
			} catch (Exception e) {
			Log.e(TAG, "💥 Error: " + e.getMessage(), e);
			return errorResponse(500, e.getMessage());
		}
	}
	
	public void clearCache() {
		memoryCache.clear();
		indexHtmlCache = null;
		Log.d(TAG, "🧹 Cache cleared");
	}
	
	public interface ConfigCallback {
		void onConfigure(@NonNull WebVirtConfig config);
	}
	
	private byte[] loadFromCache(String fullPath) throws IOException {
		if (fullPath.equals(config.getIndexAssetPath())) {
			if (indexHtmlCache == null) {
				synchronized (this) {
					if (indexHtmlCache == null) {
						indexHtmlCache = loadBytes(fullPath);
						Log.d(TAG, "💾 Cached index.html (" + indexHtmlCache.length + " bytes)");
					}
				}
			}
			return indexHtmlCache;
		}
		
		if (config.isCacheEnabled() && isCacheable(fullPath)) {
			byte[] cached = memoryCache.get(fullPath);
			if (cached == null) {
				cached = loadBytes(fullPath);
				memoryCache.put(fullPath, cached);
				Log.d(TAG, "💾 Cached: " + fullPath + " (" + cached.length + " bytes)");
			}
			return cached;
		}
		
		return loadBytes(fullPath);
	}
	
	private byte[] loadBytes(String path) throws IOException {
		InputStream is = null;
		ByteArrayOutputStream baos = null;
		try {
			is = config.getContext().getAssets().open(path);
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int read;
			while ((read = is.read(buffer)) != -1) {
				baos.write(buffer, 0, read);
			}
			return baos.toByteArray();
			} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { /* ignorar */ }
			}
			if (baos != null) {
				try { baos.close(); } catch (IOException e) { /* ignorar */ }
			}
		}
	}
	
	private WebResourceResponse buildResponse(String path, byte[] data) {
		String mimeType = getMimeType(path);
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", mimeType + "; charset=utf-8");
		headers.put("Access-Control-Allow-Origin", "*");
		
		if (path.endsWith(".html")) {
			headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.put("Pragma", "no-cache");
			headers.put("Expires", "0");
			} else {
			headers.put("Cache-Control", "public, max-age=31536000, immutable");
		}
		
		return new WebResourceResponse(mimeType, "UTF-8", 200, "OK",
		headers, new ByteArrayInputStream(data));
	}
	
	private String extractPath(String url) {
		String path = url.replace("https://", "")
		.replace("http://", "")
		.replace(config.getVirtualHost() + "/", "")
		.replace(config.getVirtualHost(), "");
		if (path.contains("?")) path = path.substring(0, path.indexOf("?"));
		if (path.contains("#")) path = path.substring(0, path.indexOf("#"));
		if (path.startsWith("/")) path = path.substring(1);
		return path;
	}
	
	private boolean isCacheable(String path) {
		return path.endsWith(".js") || path.endsWith(".css") ||
		path.endsWith(".json") || path.endsWith(".wasm") ||
		path.endsWith(".woff2");
	}
	
	private boolean isSpaRoute(String path) {
		return !path.contains(".");
	}
	
	private void validatePath(String path) {
		if (path.isEmpty() || path.contains("..") ||
		path.contains("\\") || path.startsWith("/")) {
			throw new SecurityException("Invalid path: " + path);
		}
	}
	
	private String getMimeType(String filename) {
		int dot = filename.lastIndexOf('.');
		if (dot > 0) {
			String ext = filename.substring(dot + 1).toLowerCase();
			return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
		}
		return "text/html";
	}
	
	private WebResourceResponse errorResponse(int code, String message) {
		String html = "<!DOCTYPE html><html><head><meta charset='utf-8'>" +
		"<title>Error " + code + "</title></head>" +
		"<body style='font-family:sans-serif;padding:2rem'>" +
		"<h1>WebVirt Error " + code + "</h1><p>" + message + "</p>" +
		"<hr><small>WebVirt v1.0.0</small></body></html>";
		return new WebResourceResponse("text/html", "UTF-8", code,
		"Error", new HashMap<>(),
		new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
	}
}