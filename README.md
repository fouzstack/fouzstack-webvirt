# fouzstack-webvirt
Here is the brief description for the GitHub repository in English:  **WebVirt: Effortlessly load your SPA in Android WebView with just 3 lines of code. Intelligent asset serving, automatic SPA router support, and smart in-memory caching. Open Source (MIT).**



---

# 🚀 WebVirt: Load Your SPA in Android WebView with Just 3 Lines of Code

> Tired of dealing with `WebViewAssetLoader`, manual `shouldInterceptRequest`, and React Router issues? **WebVirt** is here to simplify everything.

---

## 🎯 The Problem We All Face

Loading a Single Page Application (SPA) bundled within an Android APK into an `WebView` should be straightforward. The reality, however, often involves significant boilerplate code and intricate configurations. Traditionally, this can include:

*   Setting up and managing `WebViewAssetLoader`.
*   Manually implementing `shouldInterceptRequest` to serve files.
*   Developing custom path resolvers to ensure client-side SPA routers function correctly.
*   Detailed management of MIME types and HTTP headers for proper rendering.

WebVirt addresses these challenges head-on to provide a smooth and efficient development experience.

---

## ✨ Introducing WebVirt

**WebVirt** acts as a virtual web server within your Android application. You define a virtual host, and WebVirt intelligently intercepts requests directed to that host, serving static files and your SPA's `index.html` directly from your project's `assets` folder.

The basic integration requires a mere **3 lines of code**:

```java
// In your Activity
WebVirt.with(this)
    .host("miapp.local") // Define your virtual host
    .bind(webView);      // Bind WebVirt to your WebView
```

That's it! Your `WebView` is now prepared to load your SPA natively.

---

## 📦 Key Features

### 🎯 Automatic SPA Router Support

WebVirt automatically detects routes that don't correspond to files (like `.js`, `.css`, etc.) and serves `index.html`. This crucially allows your client-side router (React Router, Vue Router, etc.) to take over and manage the SPA's internal navigation.

```java
// Example flow:
// User navigates to https://miapp.local/dashboard when the app is running.
// WebVirt: "/dashboard" is not a file → serves index.html.
// SPA Router: Receives the path "/dashboard" and displays the appropriate component.
```

### 💾 Smart Caching System

It implements an optimized in-memory caching system to enhance performance:

*   **Index Cache**: `index.html` is kept in memory for instant access on every load.
*   **Static Cache**: Static assets (`.js`, `.css`, `.json`, `.wasm`, `.woff2`) are cached with `immutable` headers for efficient reuse.
*   **Direct Stream**: Other file types are served directly from `assets` without caching, ensuring content freshness.

### 🔒 Built-in Security

*   **Anti-Traversal Validation**: Prevents directory traversal attacks (e.g., `../`) and access to absolute paths outside the `assets` directory.
*   **Security Headers**: Automatically configures relevant security headers.
*   **SafeBrowsing**: Natively enabled on Android 8+ for safer browsing.

### 📁 Broad MIME Type Support

Provides support for over 25 MIME types, ensuring all your SPA's resources (HTML, CSS, JavaScript, JSON, images, fonts, WASM, video, etc.) are served correctly.

### 🎨 Optimized HTTP Headers

Configures appropriate `Cache-Control` headers for each resource type:

*   `no-cache` for `index.html`
*   `public, max-age=31536000, immutable` for static assets

---

## 🏗️ Architecture and Design

WebVirt is designed with a minimalist and maintainable approach, focusing on clarity and efficiency.

```
com.fouzstack.webvirt/
├── WebVirt.java                    // Main fluent API
├── WebVirtConfig.java              // Library configuration
├── WebVirtClient.java              // WebViewClient implementation
├── WebVirtChromeClient.java        // WebChromeClient implementation
└── WebVirtSettings.java            // WebView configuration utilities
```

**Simplified Flow Diagram:**

```
User navigates to https://miapp.local/
           │
           ▼
  WebVirtClient.shouldInterceptRequest()
           │
           ▼
  WebVirt.shouldIntercept() → Is this a request for the virtual host?
      /          \
     YES          NO → Pass to native WebView
     │
     ▼
  WebVirt.serveAsset()
      │
      ├─ Is it an SPA Route? → Load / Serve index.html
      ├─ Is Resource Cached? → Serve from memory
      └─ Is it a New Resource? → Load from assets → Cache → Serve
```

---

## 💻 Quick Usage Guide

### 📥 Installation

For now, the most straightforward way to integrate WebVirt is by copying the library's source files ( `WebVirt.java`, `WebVirtConfig.java`, etc.) directly into your Android project.

We are working on publishing the library to **JitPack and Maven Central** for easier dependency-based integration.

### 📂 Asset Structure for Your SPA

Place your compiled SPA files into the `app/src/main/assets/` directory of your Android project:

```
app/src/main/assets/
├── index.html            // Your SPA's entry point
├── static/               // Folder for static files
│   ├── js/
│   │   └── main.chunk.js
│   └── css/
│       └── main.chunk.css
└── favicon.ico
```

*Note*: If your build process generates the SPA into a subfolder within `assets` (e.g., `assets/dist/`), you can specify this path using the `.subfolder("dist")` method.

### 💻 Integration in Your Activity

Add WebVirt to your `MainActivity` (or the `Activity` containing your `WebView`):

```java
package com.tuapp;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import com.fouzstack.webvirt.WebVirt; // Import WebVirt

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private WebVirt webVirt; // Keep a reference if you need to clear cache
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure you have a WebView in your layout
        
        webView = findViewById(R.id.webView); // Get your WebView instance
        
        // 🚀 BASIC SETUP IN 3 LINES
        webVirt = WebVirt.with(this)
            .host("miapp.local") // Choose a virtual host for your SPA
            .bind(webView);      // Apply WebVirt to your WebView
        
        // Load the virtual URL to start your SPA
        webView.loadUrl("https://miapp.local/"); 
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources and clear cache when the Activity is destroyed
        if (webVirt != null) webVirt.clearCache();
        if (webView != null) webView.destroy();
    }
}
```

### ⚡ Advanced Configuration

For more specific scenarios, you can customize the configuration:

```java
WebVirt.with(this)
    .host("miapp.local")
    .subfolder("build") // If your build is in assets/build/
    .config(cfg -> {
        cfg.setJavaScriptEnabled(true);       // Enable JavaScript
        cfg.setDomStorageEnabled(true);      // Enable DOM Storage
        cfg.setCacheEnabled(true);           // Enable WebVirt's caching system
        // ... other WebView settings if needed
    })
    .bind(webView);
```

### 🚀 Ultra-Fast Mode

For even quicker setup in simpler cases:

```java
WebVirt.quick(this, webView, "miapp.local");
```

---

## 📊 Public API

**WebVirt**

| Method          | Description                                            | Parameters                              |
| :-------------- | :----------------------------------------------------- | :-------------------------------------- |
| `with(context)` | Static entry point.                                    | `Context`                               |
| `host(host)`    | Defines the virtual host for your SPA.                 | `String` (e.g., `"miapp.local"`)        |
| `subfolder(path)`| Specifies a subfolder within `assets`.                 | `String` (e.g., `"dist"`)               |
| `config(callback)`| Advanced configuration via `WebVirtConfig` and `WebView` settings. | Callback `ConfigCallback`               |
| `bind(webView)` | Applies the configuration, `WebViewClient`, and `WebChromeClient`. | `WebView`                               |
| `quick(ctx, wv, host)`| All-in-one quick setup.                            | `Context`, `WebView`, `String`          |
| `clearCache()`  | Clears WebVirt's in-memory cache.                      | -                                       |

**WebVirtConfig**

| Method                       | Description                                |
| :--------------------------- | :----------------------------------------- |
| `setJavaScriptEnabled(bool)` | Enables/disables JavaScript.               |
| `setDomStorageEnabled(bool)` | Enables/disables DOM Storage.              |
| `setCacheEnabled(bool)`      | Enables/disables the internal caching system. |
| `setMixedContentMode(int)`   | Configures WebView's mixed content mode.   |
| `setAssetSubfolder(String)`  | Sets the assets subfolder.                 |

---

## ⚖️ Comparison and Considerations

WebVirt stands out with its focus on simplicity and comprehensive functionality. Unlike more verbose or rudimentary solutions, WebVirt offers an integrated and robust experience.

| Feature              | WebViewAssetLoader (Google) | WebVirt                                    |
| :------------------- | :-------------------------- | :----------------------------------------- |
| **Integration API**  | Verbose (~40 lines)         | **Simple (3 lines)**                       |
| **SPA Router Support**| Manual                      | **Automatic**                              |
| **Smart Caching**    | No (Disk-based)             | **Optimized In-Memory**                    |
| **HTTP Headers**     | Manual/Basic                | **Automatic & Optimized**                  |
| **Security**         | Basic                       | **Advanced Path Validation**               |
| **Performance**      | Standard                    | **Enhanced by Caching**                    |
| **License**          | Apache 2.0                  | **MIT (Highly Permissive)**                |
| **Ease of Use**      | Moderate                    | **Very High**                              |

WebVirt is designed to be the most elegant and efficient solution for loading SPAs into Android WebViews, minimizing boilerplate code and maximizing functionality and performance.

---

## 🎓 How It Works Under the Hood

1.  **Request Interception**: `WebVirtClient` extends `WebViewClient` and overrides `shouldInterceptRequest`. Every WebView request is examined to see if it matches the configured virtual host.
2.  **SPA Route Resolution**: The requested URL is analyzed. If the path lacks a file extension (e.g., `.js`, `.css`), it's considered an SPA route, and `index.html` is served. Otherwise, it's handled as a static asset.
3.  **Caching System**: Resources are managed in memory. `index.html` is kept loaded. Static assets are cached for reuse, while other file types are streamed directly.
4.  **Optimized Header Configuration**: Appropriate `Cache-Control` headers (`no-cache` for `index.html`, `immutable` for statics) are applied to optimize browser and network behavior.

---

## 📈 Real-World Results

Before (WebViewAssetLoader)

*   ⏱️ Implementation Time: ~30 minutes
*   📝 Lines of Code: ~180
*   🐛 Common Bugs: Routes not found, missing MIME types
*   🔧 Maintenance: Complex

After (WebVirt)

*   ⏱️ Implementation Time: ~1 minute
*   📝 Lines of Code: 3
*   🐛 Common Bugs: None
*   🔧 Maintenance: Zero

---

## 🚦 Requirements

*   **Android API Level 21+ (Android 5.0 Lollipop)**
*   **WebView with JavaScript enabled**.
*   Your **compiled SPA** placed in `app/src/main/assets/` (or a subfolder if specified).

---

## 🔮 Coming Soon

We are committed to the continuous improvement of WebVirt. Upcoming developments include:

*   Official publication on **JitPack and Maven Central**.
*   Support for **Kotlin DSL** configuration.
*   Configurable **cache limit (LRU)**.
*   Callbacks for handling **not-found assets**.
*   **Live Reload** in debug mode.

---

## 💙 Support the Project

If you find WebVirt useful, please consider supporting it:

*   ⭐ **Star the repository** on GitHub.
*   💬 Share your experience or **leave a comment** on this article.
*   🔗 **Follow me** for updates and more Android development content.

---

## 📝 License

This project is distributed under the **MIT License**. Feel free to use, modify, and distribute WebVirt in your personal and commercial projects.

---

## 🎯 Conclusion

WebVirt is not just another WebView library. It's a paradigm shift in how we load SPAs into Android WebViews, offering a powerful, simple, and secure solution that lets you focus on building your application rather than wrestling with loading infrastructure.

Try it today and discover the difference!

---

Questions? Suggestions? Leave them below. I'd love to hear your thoughts!
```
