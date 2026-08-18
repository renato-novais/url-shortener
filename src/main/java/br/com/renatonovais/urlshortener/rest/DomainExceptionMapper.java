package br.com.renatonovais.urlshortener.rest;

import br.com.renatonovais.urlshortener.exception.AliasAlreadyInUseException;
import br.com.renatonovais.urlshortener.exception.InvalidUrlException;
import br.com.renatonovais.urlshortener.exception.ShortUrlNotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class DomainExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOGGER = Logger.getLogger(DomainExceptionMapper.class.getName());

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof InvalidUrlException) {
            return build(Response.Status.BAD_REQUEST, exception.getMessage());
        }
        if (exception instanceof ShortUrlNotFoundException) {
            return build(Response.Status.NOT_FOUND, exception.getMessage());
        }
        if (exception instanceof AliasAlreadyInUseException) {
            return build(Response.Status.CONFLICT, exception.getMessage());
        }
        LOGGER.log(Level.SEVERE, "Unexpected error handling request", exception);
        return build(Response.Status.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private Response build(Response.Status status, String message) {
        return Response.status(status)
                .entity(new ErrorResponse(message))
                .build();
    }
}
