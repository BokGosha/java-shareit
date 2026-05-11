package ru.practicum.shareit.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private ErrorHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ErrorHandler();
    }

    @Test
    void handleNotFoundException_returnsErrorMessage() {
        ErrorResponse r = handler.handleNotFoundException(new NotFoundException("missing"));

        assertThat(r.error()).isEqualTo("missing");
    }

    @Test
    void handleBadRequestException_returnsErrorMessage() {
        ErrorResponse r = handler.handleBadRequestException(new BadRequestException("bad"));

        assertThat(r.error()).isEqualTo("bad");
    }

    @Test
    void handleMissingRequestHeaderException_returnsHeaderMessage() throws Exception {
        Method method = Sample.class.getMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-Sharer-User-Id", parameter);

        ErrorResponse r = handler.handleMissingRequestHeaderException(ex);

        assertThat(r.error()).contains("X-Sharer-User-Id");
    }

    @Test
    void handleForbiddenException_owner_returnsErrorMessage() {
        ErrorResponse r = handler.handleForbiddenException(new UserIsNotOwnerException("not owner"));

        assertThat(r.error()).isEqualTo("not owner");
    }

    @Test
    void handleForbiddenException_booker_returnsErrorMessage() {
        ErrorResponse r = handler.handleForbiddenException(new UserIsNotBookerException("not booker"));

        assertThat(r.error()).isEqualTo("not booker");
    }

    @Test
    void handleEmailAlreadyExistsException_returnsErrorMessage() {
        ErrorResponse r = handler.handleEmailAlreadyExistsException(
                new EmailAlreadyExistsException("dup"));

        assertThat(r.error()).isEqualTo("dup");
    }

    @Test
    void handleInternalServerError_wrapsThrowableMessage() {
        ErrorResponse r = handler.handleInternalServerError(new RuntimeException("boom"));

        assertThat(r.error()).contains("boom");
    }

    @SuppressWarnings("unused")
    private static class Sample {
        public void sample(String header) {
        }
    }
}
