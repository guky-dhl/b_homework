package boku.business.account.user;

import boku.business.account.withdrawal.Withdrawal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {
    final Transaction withdrawal = Transaction.withdrawal(new User.UserId(), BigDecimal.TEN, new Withdrawal.WithdrawalId());

    @Test
    void should_complete_withdrawal() {
        var subject = withdrawal.complete_withdrawal();

        assertEquals(subject.withdrawalId, withdrawal.withdrawalId);
        assertEquals(subject.amount, withdrawal.amount);
        assertEquals(subject.type, Transaction.Type.WITHDRAWAL_DONE);
        assertEquals(subject.userId, withdrawal.userId);
    }

    @Test
    void should_fail_withdrawal() {
        var subject = withdrawal.fail_withdrawal();

        assertEquals(subject.withdrawalId, withdrawal.withdrawalId);
        assertEquals(subject.amount, withdrawal.amount);
        assertEquals(subject.type, Transaction.Type.WITHDRAWAL_FAILED);
        assertEquals(subject.userId, withdrawal.userId);
    }
}