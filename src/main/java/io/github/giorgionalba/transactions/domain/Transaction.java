package io.github.giorgionalba.transactions.domain;

public record Transaction(long id, double amount, String type, Long parentId) {

    public boolean hasParent() {
        return parentId != null;
    }
}
