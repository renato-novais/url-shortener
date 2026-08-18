package br.com.renatonovais.urlshortener.rest;

import br.com.renatonovais.urlshortener.domain.ShortUrl;

public class ShortUrlResponse {

    private final String code;
    private final String originalUrl;
    private final String shortUrl;

    public ShortUrlResponse(ShortUrl entity, String baseUri) {
        this.code = entity.getCode();
        this.originalUrl = entity.getOriginalUrl();
        this.shortUrl = baseUri + "r/" + entity.getCode();
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }
}
