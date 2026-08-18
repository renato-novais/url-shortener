package br.com.renatonovais.urlshortener.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlValidatorTest {

    @Test
    void acceptsHttpAndHttpsUrls() {
        assertTrue(UrlValidator.isValid("http://example.com"));
        assertTrue(UrlValidator.isValid("https://example.com/path?query=1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "file:///etc/passwd", "not a url", "ftp://example.com"})
    void rejectsNonHttpProtocolsAndMalformedInput(String input) {
        assertFalse(UrlValidator.isValid(input));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void rejectsNullAndEmpty(String input) {
        assertFalse(UrlValidator.isValid(input));
    }
}
