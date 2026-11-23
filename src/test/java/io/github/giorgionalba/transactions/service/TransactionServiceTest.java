package io.github.giorgionalba.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.giorgionalba.transactions.domain.ParentTransactionNotFoundException;
import io.github.giorgionalba.transactions.domain.Transaction;
import io.github.giorgionalba.transactions.domain.TransactionAlreadyExistsException;
import io.github.giorgionalba.transactions.domain.TransactionNotFoundException;
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

    @Test
    void shouldRejectTransactionWhenIdAlreadyExists() {
        // given
        service.create(new Transaction(10L, 5000.0, "cars", null));

        // when / then
        assertThatThrownBy(() -> service.create(new Transaction(10L, 1.0, "shopping", null)))
                .isInstanceOf(TransactionAlreadyExistsException.class)
                .hasMessage("Transaction 10 already exists");
    }

    @Test
    void shouldStoreTransactionWhenParentExists() {
        // given
        service.create(new Transaction(10L, 5000.0, "cars", null));

        // when
        service.create(new Transaction(11L, 10000.0, "shopping", 10L));

        // then
        assertThat(repository.existsById(11L)).isTrue();
    }

    @Test
    void shouldRejectTransactionWhenParentDoesNotExist() {
        // when / then
        assertThatThrownBy(() -> service.create(new Transaction(11L, 10000.0, "shopping", 99L)))
                .isInstanceOf(ParentTransactionNotFoundException.class)
                .hasMessage("Parent transaction 99 does not exist");
        assertThat(repository.existsById(11L)).isFalse();
    }

    @Test
    void shouldSumOnlyItsOwnAmountWhenTransactionHasNoChildren() {
        // given
        service.create(new Transaction(10L, 5000.0, "cars", null));

        // when / then
        assertThat(service.sumWithDescendants(10L)).isEqualTo(5000.0);
    }

    @Test
    void shouldSumTransactionWithAllItsDescendants() {
        // given
        service.create(new Transaction(10L, 5000.0, "cars", null));
        service.create(new Transaction(11L, 10000.0, "shopping", 10L));
        service.create(new Transaction(12L, 5000.0, "shopping", 11L));
        service.create(new Transaction(13L, 200.0, "shopping", 10L));

        // when / then
        assertThat(service.sumWithDescendants(10L)).isEqualTo(20200.0);
    }

    @Test
    void shouldSumDeepChainWithoutStackOverflow() {
        // given
        int depth = 100_000;
        service.create(new Transaction(1L, 1.0, "chain", null));
        for (long id = 2; id <= depth; id++) {
            service.create(new Transaction(id, 1.0, "chain", id - 1));
        }

        // when / then
        assertThat(service.sumWithDescendants(1L)).isEqualTo(depth);
    }

    @Test
    void shouldRejectSumWhenTransactionDoesNotExist() {
        assertThatThrownBy(() -> service.sumWithDescendants(99L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction 99 does not exist");
    }
}
