package io.github.giorgionalba.transactions.domain;

/**
 * Port for storing transactions. Implementations must be thread-safe.
 */
public interface TransactionRepository {

    /**
     * Stores the transaction only if no transaction with the same id exists.
     * <p>
     * The check and the insertion must be a single atomic operation: when several
     * threads save the same id concurrently, exactly one of them succeeds.
     *
     * @return {@code true} if the transaction was stored, {@code false} if the id already existed
     */
    boolean saveIfAbsent(Transaction transaction);

    boolean existsById(long id);
}
