package br.com.renatonovais.urlshortener.repository;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortUrlRepositoryTest {

    private EntityManagerFactory entityManagerFactory;
    private ShortUrlRepository repository;

    @BeforeEach
    void setUp() {
        entityManagerFactory = new HibernatePersistenceProvider().createEntityManagerFactory("urlShortenerPU", null);

        repository = new ShortUrlRepository(entityManagerFactory);
    }

    @AfterEach
    void tearDown() {
        entityManagerFactory.close();
    }

    @Test
    void savesAndFindsShortUrlByCode() {
        ShortUrl shortUrl = new ShortUrl("abc123", "https://example.com");

        repository.save(shortUrl);

        Optional<ShortUrl> found = repository.findByCode("abc123");

        assertTrue(found.isPresent());
        assertEquals("https://example.com", found.get().getOriginalUrl());
    }

    @Test
    void findByCodeReturnsEmptyWhenCodeDoesNotExist() {
        Optional<ShortUrl> found = repository.findByCode("doesNotExist");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByCodeReflectsWhetherCodeWasSaved() {
        repository.save(new ShortUrl("xyz789", "https://example.org"));

        assertTrue(repository.existsByCode("xyz789"));
        assertFalse(repository.existsByCode("notSaved"));
    }
}
