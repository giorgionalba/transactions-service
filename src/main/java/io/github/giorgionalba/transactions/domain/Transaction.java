package io.github.giorgionalba.transactions.domain;

/**
 * An immutable transaction. Once stored it is never modified or deleted.
 *
 * @param id       unique identifier of the transaction
 * @param amount   amount of the transaction; zero and negative values are allowed
 * @param type     category of the transaction (case-sensitive)
 * @param parentId id of the parent transaction, or {@code null} if it has no parent
 */
public record Transaction(long id, double amount, String type, Long parentId) {

    public boolean hasParent() {
        return parentId != null;
    }
}
