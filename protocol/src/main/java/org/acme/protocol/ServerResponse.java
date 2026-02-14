package org.acme.protocol;

public record ServerResponse(String response, boolean success) implements Response {
}
