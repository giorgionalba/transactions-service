package io.github.giorgionalba.transactions.service;

import io.github.giorgionalba.transactions.domain.ParentTransactionNotFoundException;
import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionAlreadyExistsException;
import io.github.giorgionalba.transactions.domain.TransactionNotFoundException;
import io.github.giorgionalba.transactions.domain.TransactionRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public void create(Transaction transaction) {
        requireParentExists(transaction);
        if (!repository.saveIfAbsent(transaction)) {
            throw new TransactionAlreadyExistsException(transaction.id());
        }
    }

    public List<Long> findIdsByType(String type) {
        return repository.findIdsByType(type);
    }

    public double sumWithDescendants(long id) {
        double total = 0;
        Deque<Long> pendingIds = new ArrayDeque<>();
        pendingIds.push(id);
        while (!pendingIds.isEmpty()) {
            long currentId = pendingIds.pop();
            total += findExisting(currentId).amount();
            repository.findChildrenIds(currentId).forEach(pendingIds::push);
        }
        return total;
    }

    private Transaction findExisting(long id) {
        return repository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    private void requireParentExists(Transaction transaction) {
        if (transaction.hasParent() && !repository.existsById(transaction.parentId())) {
            throw new ParentTransactionNotFoundException(transaction.parentId());
        }
    }
}
