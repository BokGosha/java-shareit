package ru.practicum.shareit.exception;

public class UserIsNotOwnerException extends RuntimeException {

    public UserIsNotOwnerException(String message) {
        super(message);
    }
}
