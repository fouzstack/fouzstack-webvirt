# WebVirt

**Carga tu SPA (React, Vue, Angular, Svelte, etc.) en Android WebView desde assets con 3 líneas de código.**

WebVirt convierte tu APK en un servidor web virtual. Sirve tu aplicación SPA empaquetada en `assets/` con soporte automático para client-side routing, caché inteligente, MIME types, y seguridad offline-first por defecto.

[![](https://jitpack.io/v/fouzstack/webvirt.svg)](https://jitpack.io/#fouzstack/webvirt)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-19-green.svg)]()

---

## 🚀 Instalación

### Paso 1: Añade JitPack a tu proyecto

En `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Paso 2: Añade la dependencia

En build.gradle (módulo app):

```groovy
dependencies {
    implementation 'com.github.fouzstack:webvirt:1.0.0'
}
```

---

📖 Uso

Uso básico (3 líneas)

```java
WebVirt webVirt = WebVirt.with(this)
    .host("miapp.local")
    .bind(webView);

webView.loadUrl("https://miapp.local/");
```

O aún más rápido:

```java
WebVirt.quick(this, webView, "miapp.local");
```

Uso con APIs externas

Si tu SPA necesita acceder a APIs o CDNs externos de forma segura:

```java
WebVirt.with(this)
    .host("miapp.local")
    .allowExternalDomains("api.miapp.com", "cdn.miapp.com")
    .bind(webView);
```

Modo offline estricto

Para bloquear todas las peticiones externas (apps 100% locales):

```java
WebVirt.with(this)
    .host("miapp.local")
    .offlineOnly(true)
    .bind(webView);
```

Configuración avanzada

```java
WebVirt.with(this)
    .host("miapp.local")
    .subfolder("dist")
    .allowExternalDomains("api.example.com", "fonts.googleapis.com")
    .config(cfg -> {
        cfg.setJavaScriptEnabled(true);
        cfg.setDomStorageEnabled(true);
        cfg.setCacheEnabled(true);
    })
    .bind(webView);
```

---

🔒 Seguridad

WebVirt está diseñado con seguridad offline-first por defecto:

Característica Comportamiento
Tráfico externo Bloqueado por defecto. Solo permite el host virtual.
Whitelist Solo los dominios registrados con allowExternalDomains() pueden acceder a internet.
Modo offline offlineOnly(true) bloquea cualquier intento de conexión externa.
Directory traversal Protección contra ../, rutas absolutas y caracteres ilegales.
Headers de caché Cache-Control correcto: no-cache para HTML, immutable para assets estáticos.
CORS Cabecera Access-Control-Allow-Origin: * incluida automáticamente.

Recomendaciones de seguridad:

· Usa siempre cfg.setAllowFileAccess(false) y cfg.setAllowContentAccess(false) en el callback de configuración avanzada.
· Combina WebVirt con una librería Bridge para funcionalidades nativas (cámara, almacenamiento, etc.).
· Habilita Safe Browsing en tu AndroidManifest.xml.

---

✨ Características

· API ultra simple: Solo 3 líneas para la mayoría de casos.
· SPA Router automático: Sirve index.html para rutas sin extensión (React Router, Vue Router, Angular Router, etc.).
· Caché inteligente en memoria: Doble nivel para index.html y assets estáticos con políticas diferenciadas.
· MIME types: Soporte para más de 25 formatos (JS, CSS, JSON, imágenes, fuentes, WASM, video, etc.).
· Headers HTTP óptimos: Cache-Control, Content-Type y Access-Control-Allow-Origin configurados automáticamente.
· Seguridad integrada: Protección contra directory traversal, control de peticiones externas con whitelist y modo offline estricto.
· Soporte para subcarpetas: Compatible con estructuras dist/, build/, etc.
· Limpieza de caché: Método clearCache() para liberar memoria en onDestroy().

---

📁 Estructura de Assets recomendada

```
app/src/main/assets/
├── index.html
├── static/
│   ├── js/
│   └── css/
├── manifest.json
└── favicon.ico
```

---

📚 API de referencia

Método Descripción
WebVirt.with(Context) Punto de entrada principal
.host(String) Define el host virtual (obligatorio)
.subfolder(String) Subcarpeta dentro de assets/ (opcional)
.allowExternalDomains(String...) Whitelist de dominios externos permitidos
.offlineOnly(boolean) Bloquea todo el tráfico externo
.config(ConfigCallback) Configuración avanzada del WebView
.bind(WebView) Aplica la configuración y asigna los clients
WebVirt.quick(Context, WebView, String) Configuración ultra-rápida
.clearCache() Limpia la caché en memoria
.getBaseUrl() Retorna la URL base virtual

---

🔧 ConfigCallback

Interfaz para configuración avanzada del WebView:

```java
public interface ConfigCallback {
    void onConfigure(@NonNull WebVirtConfig config);
}
```

WebVirtConfig - Opciones disponibles

Getter Setter Default
isJavascriptEnabled() setJavaScriptEnabled(boolean) true
isDomStorageEnabled() setDomStorageEnabled(boolean) true
isCacheEnabled() setCacheEnabled(boolean) true
getMixedContentMode() setMixedContentMode(int) MIXED_CONTENT_ALWAYS_ALLOW
getVirtualHost() setVirtualHost(String) null
getAssetSubfolder() setAssetSubfolder(String) ""
isOfflineOnly() setOfflineOnly(boolean) false
getAllowedExternalDomains() addAllowedExternalDomain(String) []

---

🧩 Ejemplo completo en MainActivity

```java
public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private WebVirt webVirt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        webVirt = WebVirt.with(this)
            .host("app.local")
            .allowExternalDomains("api.miapp.com")
            .config(cfg -> {
                cfg.setCacheEnabled(true);
                cfg.setJavaScriptEnabled(true);
            })
            .bind(webView);

        webView.loadUrl(webVirt.getBaseUrl());
    }

    @Override
    protected void onDestroy() {
        if (webVirt != null) webVirt.clearCache();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
```

---

📦 Requisitos

· Min SDK: 19 (Android 4.4 KitKat)
· AndroidX
· Compile SDK: 34+

---

📄 Licencia

MIT License. Ver archivo LICENSE.

---

👤 Autor

Fouzstack

---

🌟 ¿Por qué WebVirt?

Hacerlo manualmente Con WebVirt
~100 líneas de boilerplate 3 líneas
Configurar WebViewClient manualmente Automático
Implementar caché de assets Incluido
Manejar SPA routing (Rutas sin extensión) Incluido
Configurar MIME types 25+ formatos incluidos
Seguridad de peticiones externas Offline-first por defecto
Headers HTTP correctos Automáticos

---

¿Encontraste un bug o tienes una sugerencia? ¡Abre un issue en el repositorio!