package br.com.renatonovais.urlshortener.service;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.repository.ShortUrlRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryShortUrlRepository extends ShortUrlRepository {

    private final Map<String, ShortUrl> storage = new ConcurrentHashMap<>();

    @Override
    public ShortUrl save(ShortUrl shortUrl) {
        storage.put(shortUrl.getCode(), shortUrl);
        return shortUrl;
    }

    @Override
    public Optional<ShortUrl> findByCode(String code) {
        return Optional.ofNullable(storage.get(code));
    }

    @Override
    public boolean existsByCode(String code) {
        return storage.containsKey(code);
    }
}
