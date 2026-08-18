package br.com.renatonovais.urlshortener.exception;

public class ShortUrlNotFoundException extends RuntimeException {

    public ShortUrlNotFoundException(String code) {
        super("Short URL not found for code: " + code);
    }
}
