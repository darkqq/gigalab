package com.company.gigalab.enumeration;

public enum StartupParamsEnum {
    HELP("\n-h --help\tto get info\n -p --port\tto set custom port\t --kek to get keked");

    private final String value;

    StartupParamsEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
