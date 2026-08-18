package br.com.renatonovais.urlshortener.service;

import java.net.MalformedURLException;
import java.net.URL;

public final class UrlValidator {

    private UrlValidator() {
    }

    public static boolean isValid(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            URL parsed = new URL(url);
            String protocol = parsed.getProtocol();
            return "http".equals(protocol) || "https".equals(protocol);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
