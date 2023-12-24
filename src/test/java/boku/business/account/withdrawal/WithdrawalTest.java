package boku.business.account.withdrawal;

import boku.business.account.user.User;
import boku.infra.time.Timestamp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static boku.business.account.withdrawal.WithdrawalService.WithdrawalState.COMPLETED;
import static boku.business.account.withdrawal.WithdrawalService.WithdrawalState.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WithdrawalTest {
    static final WithdrawalService.Address ADDRESS = new WithdrawalService.Address("abc");

    @Test
    void should_complete_withdrawal() {
        var subject = new Withdrawal(ADDRESS, BigDecimal.TEN, new User.UserId());

        subject.complete(COMPLETED, new Timestamp(123));

        assertEquals(COMPLETED, subject.state());
        assertEquals(new Timestamp(123), subject.completedAt());
    }

    @Test
    void should_throw_when_already_completed() {
        var subject = new Withdrawal(ADDRESS, BigDecimal.TEN, new User.UserId());

        subject.complete(FAILED, new Timestamp(123));

        assertThrows(Withdrawal.WithdrawalAlreadyCompleted.class, () -> subject.complete(COMPLETED, new Timestamp(123)));
    }
}