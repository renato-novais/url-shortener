package br.com.renatonovais.urlshortener.rest;

import br.com.renatonovais.urlshortener.domain.ShortUrl;
import br.com.renatonovais.urlshortener.service.UrlShorteningService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/urls")
public class ShortUrlResource {

    @Inject
    private UrlShorteningService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response shorten(ShortenUrlRequest request, @Context UriInfo uriInfo) {
        ShortUrl shortUrl = service.shorten(request.getUrl(), request.getAlias());
        String baseUri = uriInfo.getBaseUri().toString();
        return Response.status(Response.Status.CREATED)
                .entity(new ShortUrlResponse(shortUrl, baseUri))
                .build();
    }

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findByCode(@PathParam("code") String code, @Context UriInfo uriInfo) {
        ShortUrl shortUrl = service.findByCode(code);
        String baseUri = uriInfo.getBaseUri().toString();
        return Response.ok(new ShortUrlResponse(shortUrl, baseUri)).build();
    }
}
