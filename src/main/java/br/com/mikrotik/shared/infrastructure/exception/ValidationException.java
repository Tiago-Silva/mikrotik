package br.com.mikrotik.shared.infrastructure.exception;

/**
 * Exceção lançada quando há erros de validação de dados
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
