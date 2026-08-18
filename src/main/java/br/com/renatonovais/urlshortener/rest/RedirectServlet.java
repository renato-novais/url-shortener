package br.com.renatonovais.urlshortener.rest;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.exception.ShortUrlNotFoundException;
import br.com.renatonovais.urlshortener.service.UrlShorteningService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/r/*")
public class RedirectServlet extends HttpServlet {

    @Inject
    private UrlShorteningService service;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String code = req.getPathInfo();
        if (code == null || code.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        code = code.substring(1);

        try {
            ShortUrl shortUrl = service.findByCode(code);
            resp.sendRedirect(shortUrl.getOriginalUrl());
        } catch (ShortUrlNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}
