package io.github.giorgionalba.transactions.domain;

public class ParentTransactionNotFoundException extends RuntimeException {

    public ParentTransactionNotFoundException(long parentId) {
        super("Parent transaction " + parentId + " does not exist");
    }
}
