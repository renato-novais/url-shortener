package br.com.renatonovais.urlshortener.repository;

import br.com.renatonovais.urlshortener.domain.ShortUrl;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import java.util.Optional;

@ApplicationScoped
public class ShortUrlRepository {

    // @PersistenceUnit injeta a fábrica, não uma instância gerenciada pelo container --
    // necessário porque com persistence-unit RESOURCE_LOCAL a própria aplicação (não o
    // container) controla o ciclo de vida da transação via EntityManager.getTransaction().
    @PersistenceUnit(unitName = "urlShortenerPU")
    EntityManagerFactory entityManagerFactory; // pacote-privado: permite injeção manual em testes do mesmo pacote

    public ShortUrl save(ShortUrl shortUrl) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(shortUrl);
            entityManager.getTransaction().commit();
            return shortUrl;
        } finally {
            entityManager.close();
        }
    }

    public Optional<ShortUrl> findByCode(String code) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ShortUrl shortUrl = entityManager
                    .createQuery("SELECT s FROM ShortUrl s WHERE s.code = :code", ShortUrl.class)
                    .setParameter("code", code)
                    .getSingleResult();
            return Optional.of(shortUrl);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            entityManager.close();
        }
    }

    public boolean existsByCode(String code) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Long count = entityManager
                    .createQuery("SELECT COUNT(s) FROM ShortUrl s WHERE s.code = :code", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();
            return count > 0;
        } finally {
            entityManager.close();
        }
    }
}
