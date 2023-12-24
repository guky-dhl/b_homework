package boku.business.account.withdrawal;

import boku.business.account.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WithdrawalsTest {
    public static final int TOTAL_COUNT = 100;
    final User.UserId userId = new User.UserId();
    final Withdrawals subject = new Withdrawals();

    @BeforeEach
    void setUp() {
        for (int i = 1; i <= TOTAL_COUNT; i++) {
            subject.save(new Withdrawal(new WithdrawalService.Address("abc"), BigDecimal.valueOf(i), userId));
        }
    }

    @Test
    public void should_list_user_withdrawals() {
        var count = 20;
        var skip = 10;
        var result = subject.byUserId(userId, skip, count);

        assertEquals(count, result.size());
        for (int i = 0; i < count; i++){
            var withdrawal = result.get(i);
            assertEquals(BigDecimal.valueOf(i + skip + 1), withdrawal.amount);
        }
    }

    @Test
    public void should_count_user_withdrawals() {
        var result = subject.countByUserId(userId);

        assertEquals(TOTAL_COUNT, result);
    }
}