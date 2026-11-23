package io.github.giorgionalba.transactions.service;

import io.github.giorgionalba.transactions.domain.ParentTransactionNotFoundException;
import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionAlreadyExistsException;
import io.github.giorgionalba.transactions.domain.TransactionRepository;
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

    private void requireParentExists(Transaction transaction) {
        if (transaction.hasParent() && !repository.existsById(transaction.parentId())) {
            throw new ParentTransactionNotFoundException(transaction.parentId());
        }
    }
}
