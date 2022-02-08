package io.testcontainers.exception;

public class ProductNotFoundException extends Exception {
    public ProductNotFoundException() {
        super();
    }
    public ProductNotFoundException(String s) {
        super(s);
    }
}
