// src/main/java/org/example/exception/OlympiadNotFoundException.java
package org.example.exception;

public class OlympiadNotFoundException extends RuntimeException {
    public OlympiadNotFoundException(String message) {
        super(message);
    }
}