package br.com.renatonovais.urlshortener.rest;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.service.UrlShorteningService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/urls")
public class ShortUrlResource {

    @Inject
    private UrlShorteningService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response shorten(ShortenUrlRequest request, @Context HttpServletRequest servletRequest) {
        ShortUrl shortUrl = service.shorten(request.getUrl(), request.getAlias());
        return Response.status(Response.Status.CREATED)
                .entity(new ShortUrlResponse(shortUrl, appBaseUri(servletRequest)))
                .build();
    }

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByCode(@PathParam("code") String code, @Context HttpServletRequest servletRequest) {
        ShortUrl shortUrl = service.findByCode(code);
        return Response.ok(new ShortUrlResponse(shortUrl, appBaseUri(servletRequest))).build();
    }

    // uriInfo.getBaseUri() do JAX-RS já inclui o prefixo "/api" do JaxRsActivator,
    // mas o RedirectServlet (GET /r/{code}) fica fora dele -- por isso montamos a
    // base a partir do contexto da própria requisição HTTP, não da aplicação JAX-RS.
    private String appBaseUri(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/";
    }
}
