package org.example.security;

/**
 * Класс является кастомным исключением, которое используется для
 * обработки ошибки, связанной с удалением админом или оператором своей учётки.
 */
public class DeleteAccountException extends RuntimeException {
    public DeleteAccountException(String message) {
        super(message);
    }
}
