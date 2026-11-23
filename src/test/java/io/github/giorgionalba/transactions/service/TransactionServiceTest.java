package io.github.giorgionalba.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.persistence.InMemoryTransactionRepository;
import org.junit.jupiter.api.Test;

class TransactionServiceTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();
    private final TransactionService service = new TransactionService(repository);

    @Test
    void shouldStoreNewTransaction() {
        // when
        service.create(new Transaction(10L, 5000.0, "cars", null));

        // then
        assertThat(repository.existsById(10L)).isTrue();
    }
}
