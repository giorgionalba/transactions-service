package io.github.giorgionalba.transactions.domain;

import java.util.List;

public interface TransactionRepository {

    boolean saveIfAbsent(Transaction transaction);

    boolean existsById(long id);

    List<Long> findIdsByType(String type);
}
