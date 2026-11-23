package io.github.giorgionalba.transactions.persistence;

import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> transactionsById = new ConcurrentHashMap<>();

    @Override
    public boolean saveIfAbsent(Transaction transaction) {
        return transactionsById.putIfAbsent(transaction.id(), transaction) == null;
    }

    @Override
    public boolean existsById(long id) {
        return transactionsById.containsKey(id);
    }

    @Override
    public List<Long> findIdsByType(String type) {
        return List.of();
    }
}
