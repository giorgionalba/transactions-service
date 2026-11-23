package io.github.giorgionalba.transactions.persistence;

import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<Long, Transaction> transactionsById = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> idsByType = new ConcurrentHashMap<>();

    @Override
    public boolean saveIfAbsent(Transaction transaction) {
        if (transactionsById.putIfAbsent(transaction.id(), transaction) != null) {
            return false;
        }
        indexByType(transaction);
        return true;
    }

    @Override
    public boolean existsById(long id) {
        return transactionsById.containsKey(id);
    }

    @Override
    public List<Long> findIdsByType(String type) {
        return List.copyOf(idsByType.getOrDefault(type, Set.of()));
    }

    private void indexByType(Transaction transaction) {
        idsByType.computeIfAbsent(transaction.type(), type -> new ConcurrentSkipListSet<>())
                .add(transaction.id());
    }

    @Override
    public Optional<Transaction> findById(long id) {
        return Optional.empty();
    }

    @Override
    public List<Long> findChildrenIds(long parentId) {
        return List.of();
    }
}
