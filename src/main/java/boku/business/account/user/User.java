package boku.business.account.user;

import boku.business.account.withdrawal.WithdrawalService;
import boku.infra.persistance.Entity;
import boku.infra.persistance.Id;

import java.util.HashSet;
import java.util.Set;

public class User extends Entity<User.UserId> {
    private final Set<WithdrawalService.Address> addresses = new HashSet<>();
    private Balance balance = Balance.zero();

    public User(UserId id) {
        super(id);
    }

    public User() {
        super(new UserId());
    }

    public void apply(Transaction transaction, Transactions transactions) {
        if (!transaction.userId.equals(id)) {
            throw new UnrelatedTransaction(this, transaction);
        }
        switch (transaction.type) {
            case DEPOSIT, TRANSFER -> this.balance = balance.add(transaction.amount);
            case WITHDRAWAL_REQUESTED -> this.balance = balance.freeze(transaction.amount);
            case WITHDRAWAL_DONE -> this.balance = balance.release(transaction.amount);
            case WITHDRAWAL_FAILED -> this.balance = balance.unfreeze(transaction.amount);
        }
        transactions.save(transaction);
    }

    public Balance balance() {
        return this.balance;
    }

    public boolean belongs_to_user(WithdrawalService.Address address) {
        return addresses.contains(address);
    }

    public void add_address(WithdrawalService.Address address) {
        this.addresses.add(address);
    }

    public static final class UserId extends Id {
    }

    public static class UnrelatedTransaction extends IllegalArgumentException {
        public UnrelatedTransaction(User user, Transaction transaction) {
            super("Transaction [%s] belongs to user [%s], but was applied to user [%s]".formatted(transaction.id, transaction.userId, user.id));
        }
    }

    public static class UnknownUserAddress extends IllegalArgumentException {
        public UnknownUserAddress(User user, WithdrawalService.Address address) {
            super("Address [%s] does not belong to user [%s]".formatted(address, user.id));
        }
    }
}
