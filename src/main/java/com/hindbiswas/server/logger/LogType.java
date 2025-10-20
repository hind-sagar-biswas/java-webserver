package com.hindbiswas.server.logger;

public enum LogType {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    private String value;

    LogType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
