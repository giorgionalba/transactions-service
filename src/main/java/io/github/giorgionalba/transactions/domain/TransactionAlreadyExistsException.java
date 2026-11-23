package io.github.giorgionalba.transactions.domain;

public class TransactionAlreadyExistsException extends RuntimeException {

    public TransactionAlreadyExistsException(long id) {
        super("Transaction " + id + " already exists");
    }
}
