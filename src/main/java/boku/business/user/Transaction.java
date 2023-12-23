package boku.business.user;

import boku.business.withdrawal.WithdrawalService;
import boku.infra.persistance.Entity;
import boku.infra.persistance.Id;

import java.math.BigDecimal;
import java.util.Optional;


public class Transaction extends Entity<Transaction.TransactionId> {
    public final BigDecimal amount;
    public final Type type;
    public final User.UserId userId;
    public final Optional<WithdrawalService.Address> address;

    public static Transaction deposit(User.UserId userId, BigDecimal amount) {
        return new Transaction(amount, Type.DEPOSIT, userId);
    }

    public static Transaction withdrawal(User.UserId userId, BigDecimal amount, WithdrawalService.Address address) {
        return new Transaction(amount, Type.WITHDRAWAL_REQUESTED, userId, address);
    }

    public static Transaction transfer(User.UserId userId, BigDecimal amount) {
        return new Transaction(amount, Type.TRANSFER, userId);
    }

    Transaction(BigDecimal amount, Type type, User.UserId userId) {
        super(new TransactionId());
        this.amount = amount;
        this.type = type;
        this.userId = userId;
        this.address = Optional.empty();
    }

    Transaction(BigDecimal amount, Type type, User.UserId userId, WithdrawalService.Address address) {
        super(new TransactionId());
        this.amount = amount;
        this.type = type;
        this.userId = userId;
        this.address = Optional.of(address);
    }

    public Transaction complete_withdrawal() {
        validate_withdrawal_transaction();
        return new Transaction(amount, Type.WITHDRAWAL_DONE, userId, address.get());
    }

    public Transaction fail_withdrawal() {
        validate_withdrawal_transaction();
        return new Transaction(amount, Type.WITHDRAWAL_FAILED, userId, address.get());
    }

    private void validate_withdrawal_transaction() {
        if (this.type != Type.WITHDRAWAL_REQUESTED) {
            throw new InvalidWithdrawalTransactionType(this.type);
        }
        if (this.address.isEmpty()) {
            throw new WithdrawalMissingAddress(this.id);
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

    public static final class WithdrawalMissingAddress extends IllegalStateException {
        WithdrawalMissingAddress(Transaction.TransactionId id) {
            super("Withdrawal transaction [%s] missing address".formatted(id));
        }
    }
}
