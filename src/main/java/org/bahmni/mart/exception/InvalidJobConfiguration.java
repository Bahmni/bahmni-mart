package org.bahmni.mart.exception;

public class InvalidJobConfiguration extends RuntimeException {
    public InvalidJobConfiguration() {
        super("Invalid Job Configuration");
    }

    public InvalidJobConfiguration(String message) {
        super(message);
    }
}
