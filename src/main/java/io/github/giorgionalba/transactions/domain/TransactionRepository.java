package io.github.giorgionalba.transactions.domain;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    boolean saveIfAbsent(Transaction transaction);

    boolean existsById(long id);

    List<Long> findIdsByType(String type);

    Optional<Transaction> findById(long id);

    List<Long> findChildrenIds(long parentId);
}
