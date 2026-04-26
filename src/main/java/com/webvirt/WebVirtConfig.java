package com.webvirt;

import android.content.Context;
import android.webkit.WebSettings;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuración para WebVirt.
 */
public class WebVirtConfig {
    private Context context;
    private String virtualHost;
    private String assetSubfolder = "";
    private boolean javascriptEnabled = true;
    private boolean domStorageEnabled = true;
    private boolean cacheEnabled = true;
    private int mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;

    // === Nueva funcionalidad de seguridad ===
    private final Set<String> allowedExternalDomains = new HashSet<>();
    private boolean offlineOnly = false;

    public WebVirtConfig() {}

    // ==================== GETTERS ====================
    public Context getContext() { return context; }
    public String getVirtualHost() { return virtualHost; }
    public String getAssetSubfolder() { return assetSubfolder; }
    public boolean isJavascriptEnabled() { return javascriptEnabled; }
    public boolean isDomStorageEnabled() { return domStorageEnabled; }
    public boolean isCacheEnabled() { return cacheEnabled; }
    public int getMixedContentMode() { return mixedContentMode; }

    // Nuevos getters
    public Set<String> getAllowedExternalDomains() {
        return Collections.unmodifiableSet(allowedExternalDomains);
    }
    public boolean isOfflineOnly() { return offlineOnly; }

    // ==================== SETTERS ====================
    public void setContext(Context context) { 
        this.context = context.getApplicationContext(); 
    }

    public void setVirtualHost(String virtualHost) { 
        this.virtualHost = virtualHost; 
    }

    public void setAssetSubfolder(String subfolder) {
        String trimmed = subfolder != null ? subfolder.trim() : "";
        this.assetSubfolder = trimmed.isEmpty() ? "" : 
            (trimmed.endsWith("/") ? trimmed : trimmed + "/");
    }

    public void setJavaScriptEnabled(boolean enabled) { 
        this.javascriptEnabled = enabled; 
    }

    public void setDomStorageEnabled(boolean enabled) { 
        this.domStorageEnabled = enabled; 
    }

    public void setCacheEnabled(boolean enabled) { 
        this.cacheEnabled = enabled; 
    }

    public void setMixedContentMode(int mode) { 
        this.mixedContentMode = mode; 
    }

    // ==================== NUEVOS SETTERS ====================
    public void setOfflineOnly(boolean offlineOnly) {
        this.offlineOnly = offlineOnly;
    }

    public void addAllowedExternalDomain(String domain) {
        if (domain != null && !domain.isEmpty()) {
            this.allowedExternalDomains.add(domain.toLowerCase().trim());
        }
    }

    // ==================== HELPERS ====================
    public String getBaseUrl() { 
        return "https://" + virtualHost + "/"; 
    }

    public String getIndexAssetPath() { 
        return assetSubfolder + "index.html"; 
    }

    /**
     * Verifica si un dominio externo está permitido.
     */
    public boolean isDomainAllowed(String host) {
        if (host == null) return false;
        String lowerHost = host.toLowerCase();
        return allowedExternalDomains.contains(lowerHost);
    }
}