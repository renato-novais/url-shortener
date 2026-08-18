package br.com.renatonovais.urlshortener.repository;

import br.com.renatonovais.urlshortener.domain.ShortUrl;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@ApplicationScoped
public class ShortUrlRepository {

    @PersistenceContext(unitName = "urlShortenerPU")
    private EntityManager entityManager;

    public ShortUrl save(ShortUrl shortUrl) {
        entityManager.getTransaction().begin();
        entityManager.persist(shortUrl);
        entityManager.getTransaction().commit();
        return shortUrl;
    }

    public Optional<ShortUrl> findByCode(String code) {
        try {
            ShortUrl shortUrl = entityManager
                    .createQuery("SELECT s FROM ShortUrl s WHERE s.code = :code", ShortUrl.class)
                    .setParameter("code", code)
                    .getSingleResult();
            return Optional.of(shortUrl);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByCode(String code) {
        Long count = entityManager
                .createQuery("SELECT COUNT(s) FROM ShortUrl s WHERE s.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        return count > 0;
    }
}
