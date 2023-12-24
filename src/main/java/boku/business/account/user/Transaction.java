package boku.business.account.user;

import boku.business.account.withdrawal.Withdrawal;
import boku.infra.persistance.Entity;
import boku.infra.persistance.Id;

import java.math.BigDecimal;
import java.util.Optional;


public class Transaction extends Entity<Transaction.TransactionId> {
    public final BigDecimal amount;
    public final Type type;
    public final User.UserId userId;
    public final Optional<Withdrawal.WithdrawalId> withdrawalId;

    Transaction(BigDecimal amount, Type type, User.UserId userId) {
        super(new TransactionId());
        this.amount = amount;
        this.type = type;
        this.userId = userId;
        this.withdrawalId = Optional.empty();
    }

    Transaction(BigDecimal amount, Type type, User.UserId userId, Withdrawal.WithdrawalId withdrawalId) {
        super(new TransactionId());
        this.amount = amount;
        this.type = type;
        this.userId = userId;
        this.withdrawalId = Optional.ofNullable(withdrawalId);
    }

    public static Transaction deposit(User.UserId userId, BigDecimal amount) {
        return new Transaction(amount, Type.DEPOSIT, userId);
    }

    public static Transaction withdrawal(User.UserId userId, BigDecimal amount, Withdrawal.WithdrawalId withdrawalId) {
        return new Transaction(amount, Type.WITHDRAWAL_REQUESTED, userId, withdrawalId);
    }

    public static Transaction transfer(User.UserId userId, BigDecimal amount) {
        return new Transaction(amount, Type.TRANSFER, userId);
    }

    public Transaction complete_withdrawal() {
        validate_withdrawal_transaction();
        return new Transaction(amount, Type.WITHDRAWAL_DONE, userId, withdrawalId.get());
    }

    public Transaction fail_withdrawal() {
        validate_withdrawal_transaction();
        return new Transaction(amount, Type.WITHDRAWAL_FAILED, userId, withdrawalId.get());
    }

    private void validate_withdrawal_transaction() {
        if (this.type != Type.WITHDRAWAL_REQUESTED) {
            throw new InvalidWithdrawalTransactionType(this.type);
        }
        if (this.withdrawalId.isEmpty()) {
            throw new WithdrawalMissing(this.id);
        }
    }


    public enum Type {
        DEPOSIT,
        WITHDRAWAL_REQUESTED,
        WITHDRAWAL_DONE,
        WITHDRAWAL_FAILED,
        TRANSFER
    }

    public static final class TransactionId extends Id {
    }

    public static final class InvalidWithdrawalTransactionType extends IllegalStateException {
        InvalidWithdrawalTransactionType(Transaction.Type type) {
            super("Can finalize only transaction request, but got [%s]".formatted(type));
        }
    }

    public static final class WithdrawalMissing extends IllegalStateException {
        WithdrawalMissing(Transaction.TransactionId id) {
            super("Withdrawal transaction [%s] missing address".formatted(id));
        }
    }
}
