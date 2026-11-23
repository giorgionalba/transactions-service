package io.github.giorgionalba.transactions.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.giorgionalba.transactions.domain.Transaction;

public record TransactionRequest(
        Double amount,
        String type,
        @JsonProperty("parent_id") Long parentId) {

    public Transaction toTransaction(long id) {
        return new Transaction(id, amount, type, parentId);
    }
}
