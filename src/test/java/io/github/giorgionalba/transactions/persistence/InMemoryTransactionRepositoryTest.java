package io.github.giorgionalba.transactions.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.giorgionalba.transactions.domain.Transaction;
import org.junit.jupiter.api.Test;

class InMemoryTransactionRepositoryTest {

    private final InMemoryTransactionRepository repository = new InMemoryTransactionRepository();

    @Test
    void shouldStoreTransactionWhenIdIsNew() {
        // given
        Transaction transaction = new Transaction(10L, 5000.0, "cars", null);

        // when
        boolean saved = repository.saveIfAbsent(transaction);

        // then
        assertThat(saved).isTrue();
        assertThat(repository.existsById(10L)).isTrue();
    }

    @Test
    void shouldNotStoreTransactionWhenIdAlreadyExists() {
        // given
        repository.saveIfAbsent(new Transaction(10L, 5000.0, "cars", null));

        // when
        boolean saved = repository.saveIfAbsent(new Transaction(10L, 1.0, "shopping", null));

        // then
        assertThat(saved).isFalse();
    }

    @Test
    void shouldReportMissingTransactionAsNonExistent() {
        assertThat(repository.existsById(99L)).isFalse();
    }
}
