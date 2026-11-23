package io.github.giorgionalba.transactions.api;

import io.github.giorgionalba.transactions.domain.ParentTransactionNotFoundException;
import io.github.giorgionalba.transactions.domain.TransactionAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ParentTransactionNotFoundException.class)
    public ProblemDetail handleParentNotFound(ParentTransactionNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(TransactionAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(TransactionAlreadyExistsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
