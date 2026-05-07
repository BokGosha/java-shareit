package ru.practicum.shareit.exception;

public class UserIsNotBookerException extends RuntimeException {

    public UserIsNotBookerException(String message) {
        super(message);
    }
}
