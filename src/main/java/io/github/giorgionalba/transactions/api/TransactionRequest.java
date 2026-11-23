package io.github.giorgionalba.transactions.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.giorgionalba.transactions.domain.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
        @NotNull Double amount,
        @NotBlank String type,
        @JsonProperty("parent_id") @Positive Long parentId) {

    public Transaction toTransaction(long id) {
        return new Transaction(id, amount, type, parentId);
    }
}
