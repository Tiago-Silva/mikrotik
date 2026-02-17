package br.com.mikrotik.shared.infrastructure.exception;

public class MikrotikConnectionException extends RuntimeException {
    public MikrotikConnectionException(String message) {
        super(message);
    }

    public MikrotikConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
