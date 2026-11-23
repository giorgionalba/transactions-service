package io.github.giorgionalba.transactions.domain;

public interface TransactionRepository {

    boolean saveIfAbsent(Transaction transaction);

    boolean existsById(long id);
}
