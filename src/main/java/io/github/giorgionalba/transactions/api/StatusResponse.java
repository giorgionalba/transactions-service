package io.github.giorgionalba.transactions.api;

public record StatusResponse(String status) {

    private static final String OK = "ok";

    public static StatusResponse ok() {
        return new StatusResponse(OK);
    }
}
