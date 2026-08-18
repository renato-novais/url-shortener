package br.com.renatonovais.urlshortener.service;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.exception.AliasAlreadyInUseException;
import br.com.renatonovais.urlshortener.exception.InvalidUrlException;
import br.com.renatonovais.urlshortener.exception.ShortUrlNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlShorteningServiceTest {

    private UrlShorteningService service;

    @BeforeEach
    void setUp() {
        service = new UrlShorteningService(new InMemoryShortUrlRepository());
    }

    @Test
    void shortensValidUrlWithGeneratedCode() {
        ShortUrl result = service.shorten("https://example.com", null);

        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(7, result.getCode().length());
    }

    @Test
    void shortensUrlUsingProvidedAlias() {
        ShortUrl result = service.shorten("https://example.com", "meu-link");

        assertEquals("meu-link", result.getCode());
    }

    @Test
    void rejectsInvalidUrl() {
        assertThrows(InvalidUrlException.class, () -> service.shorten("nao-e-url", null));
    }

    @Test
    void rejectsInvalidAliasFormat() {
        assertThrows(InvalidUrlException.class, () -> service.shorten("https://example.com", "a"));
    }

    @Test
    void rejectsAliasAlreadyInUse() {
        service.shorten("https://example.com", "duplicado");

        assertThrows(AliasAlreadyInUseException.class,
                () -> service.shorten("https://outro.com", "duplicado"));
    }

    @Test
    void findByCodeReturnsPersistedShortUrl() {
        service.shorten("https://example.com", "achavel");

        ShortUrl found = service.findByCode("achavel");

        assertEquals("https://example.com", found.getOriginalUrl());
    }

    @Test
    void findByCodeThrowsWhenCodeDoesNotExist() {
        assertThrows(ShortUrlNotFoundException.class, () -> service.findByCode("naoexiste"));
    }
}
