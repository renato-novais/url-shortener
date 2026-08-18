package br.com.renatonovais.urlshortener.service;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.exception.AliasAlreadyInUseException;
import br.com.renatonovais.urlshortener.exception.InvalidUrlException;
import br.com.renatonovais.urlshortener.exception.ShortUrlNotFoundException;
import br.com.renatonovais.urlshortener.repository.ShortUrlRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.regex.Pattern;

@ApplicationScoped
public class UrlShorteningService {

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");

    @Inject
    private ShortUrlRepository repository;

    private final ShortCodeGenerator codeGenerator = new ShortCodeGenerator();

    public synchronized ShortUrl shorten(String originalUrl, String alias) {
        if (!UrlValidator.isValid(originalUrl)) {
            throw new InvalidUrlException("Invalid URL: " + originalUrl);
        }

        String code;
        if (alias != null && !alias.trim().isEmpty()) {
            if (!ALIAS_PATTERN.matcher(alias).matches()) {
                throw new InvalidUrlException(
                        "Invalid alias (use 3-30 letters, numbers, '-' or '_'): " + alias);
            }
            if (repository.existsByCode(alias)) {
                throw new AliasAlreadyInUseException(alias);
            }
            code = alias;
        } else {
            code = generateUniqueCode();
        }

        return repository.save(new ShortUrl(code, originalUrl));
    }

    public ShortUrl findByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ShortUrlNotFoundException(code));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = codeGenerator.generate();
        } while (repository.existsByCode(code));
        return code;
    }
}
