package boku.business.account.withdrawal;

import boku.business.account.user.User;
import boku.infra.persistance.Entity;
import boku.infra.persistance.Id;
import boku.infra.time.Timestamp;

import java.math.BigDecimal;
import java.util.UUID;

import static boku.business.account.withdrawal.WithdrawalService.WithdrawalState.PROCESSING;


public class Withdrawal extends Entity<Withdrawal.WithdrawalId> {
    public static final long UPDATE_INTERVAL = 5_000;

    public final WithdrawalService.WithdrawalId externalId = new WithdrawalService.WithdrawalId(UUID.randomUUID());
    public final WithdrawalService.Address address;
    public final BigDecimal amount;
    public final User.UserId userId;
    private WithdrawalService.WithdrawalState state = PROCESSING;
    private Timestamp completedAt = Timestamp.zero();

    public Withdrawal(WithdrawalId id, WithdrawalService.Address address, BigDecimal amount, User.UserId userId) {
        super(id);
        this.address = address;
        this.amount = amount;
        this.userId = userId;
    }

    public Withdrawal(WithdrawalService.Address address, BigDecimal amount, User.UserId userId) {
        super(new WithdrawalId());
        this.address = address;
        this.amount = amount;
        this.userId = userId;
    }

    public void complete(WithdrawalService.WithdrawalState newState, Timestamp when) {
        if (this.state != PROCESSING) {
            throw new WithdrawalAlreadyCompleted(this);
        }
        this.state = newState;
        this.completedAt = when;
    }

    public WithdrawalService.WithdrawalState state() {
        return this.state;
    }

    public Timestamp completedAt() {
        return this.completedAt;
    }

    public static final class WithdrawalAlreadyCompleted extends IllegalStateException {
        WithdrawalAlreadyCompleted(Withdrawal withdrawal) {
            super("Withdrawal already completed [%s]".formatted(withdrawal.id));
        }
    }

    public static final class WithdrawalId extends Id {
    }
}
