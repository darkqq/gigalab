package com.company.gigalab.enumeration;

public enum HttpCodes {
    OK(200, "OK"),
    CREATED(201, "Created"),
    NOT_FOUND(404, "Not Found"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    REDIRECT(301, "Redirect");

    private final Integer code;
    private final String description;

    HttpCodes(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
