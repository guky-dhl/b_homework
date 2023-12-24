package boku.business.user;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Transactions {
    private final ConcurrentHashMap<Transaction.TransactionId, Transaction> transactions = new ConcurrentHashMap<>();

    public Optional<Transaction> get(Transaction.TransactionId id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public void save(Transaction transaction) {
        transactions.put(transaction.id, transaction);
    }
}
