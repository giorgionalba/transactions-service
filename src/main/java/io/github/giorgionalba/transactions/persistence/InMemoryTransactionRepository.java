package io.github.giorgionalba.transactions.persistence;

import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    @Override
    public boolean saveIfAbsent(Transaction transaction) {
        return false;
    }

    @Override
    public boolean existsById(long id) {
        return false;
    }
}
