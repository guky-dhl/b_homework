package boku.business.user;

import boku.business.withdrawal.WithdrawalService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    static final Transaction withdrawal = Transaction.withdrawal(new User.UserId(), BigDecimal.TEN, new WithdrawalService.Address("test"));
    @Test
    void should_complete_withdrawal() {
        var subject = withdrawal.complete_withdrawal();

        assertEquals(subject.address, withdrawal.address);
        assertEquals(subject.amount, withdrawal.amount);
        assertEquals(subject.type, Transaction.Type.WITHDRAWAL_DONE);
        assertEquals(subject.userId, withdrawal.userId);
    }

    @Test
    void should_fail_withdrawal() {
        var subject = withdrawal.fail_withdrawal();

        assertEquals(subject.address, withdrawal.address);
        assertEquals(subject.amount, withdrawal.amount);
        assertEquals(subject.type, Transaction.Type.WITHDRAWAL_FAILED);
        assertEquals(subject.userId, withdrawal.userId);
    }
}