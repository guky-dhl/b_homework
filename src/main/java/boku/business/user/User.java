package boku.business.user;

import boku.infra.persistance.Entity;
import boku.infra.persistance.Id;

public class User extends Entity<User.UserId> {
    private Balance balance = Balance.zero();

    public User(UserId id) {
        super(id);
    }

    public void apply(Transaction transaction) {
        if (!transaction.userId.equals(id)) {
            throw new UnrelatedTransaction(this, transaction);
        }
        switch (transaction.type) {
            case DEPOSIT, TRANSFER -> this.balance = balance.add(transaction.amount);
            case WITHDRAWAL_REQUESTED -> this.balance = balance.freeze(transaction.amount);
            case WITHDRAWAL_DONE -> this.balance = balance.release(transaction.amount);
            case WITHDRAWAL_FAILED -> this.balance = balance.unfreeze(transaction.amount);
        }
    }

    public Balance balance() {
        return this.balance;
    }


    public static final class UserId extends Id {
    }

    public static class UnrelatedTransaction extends IllegalArgumentException {
        UnrelatedTransaction(User user, Transaction transaction) {
            super("Transaction [%s] belongs to user [%s], but was applied to user [%s]".formatted(transaction.id, transaction.userId, user.id));
        }
    }
}
