package boku.business.account.withdrawal.actions;

import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.user.Users;
import boku.business.account.withdrawal.Withdrawal;
import boku.business.account.withdrawal.WithdrawalService;
import boku.business.account.withdrawal.WithdrawalService.Address;
import boku.business.account.withdrawal.Withdrawals;
import boku.infra.job.JobService;
import boku.infra.thread.ThreadUtils;
import boku.infra.time.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WithdrawTest {

    private final Address address = new Address("abc");
    private final User user = new User();
    private final Users users = new Users();
    private final Transactions transactions = new Transactions();
    private final Withdrawals withdrawals = new Withdrawals();
    private final WithdrawalService withdrawalService = mock(WithdrawalService.class);
    private final JobService jobService = mock(JobService.class);
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final Withdraw.Handler subject = new Withdraw.Handler(users, transactions, withdrawals, withdrawalService, jobService, clock);

    @BeforeEach
    void setup() {
        users.save(user);
        user.add_address(address);
        user.apply(Transaction.deposit(user.id, BigDecimal.TEN), transactions);
    }

    @Test
    public void should_withdraw_and_schedule_for_status_update() {
        var withdraw = new Withdraw(user.id, address, BigDecimal.TEN);

        var result = subject.handle(withdraw);

        var withdrawal = withdrawals.get(result.id());
        assertEquals(address, withdrawal.address);
        assertEquals(user.id, withdrawal.userId);
        assertEquals(BigDecimal.TEN, withdrawal.amount);

        var transaction = transactions.all().stream().filter((it) -> it.type == Transaction.Type.WITHDRAWAL_REQUESTED).findFirst().get();
        assertEquals(Transaction.Type.WITHDRAWAL_REQUESTED, transaction.type);
        assertEquals(BigDecimal.TEN, transaction.amount);
        assertEquals(withdrawal.id, transaction.withdrawalId.get());

        assertEquals(BigDecimal.ZERO, user.balance().free());
        assertEquals(BigDecimal.TEN, user.balance().frozen());

        verify(withdrawalService).requestWithdrawal(withdrawal.externalId, withdrawal.address, BigDecimal.TEN);
        verify(jobService).scheduleCommand(new UpdateWithdrawalState(user, transaction, withdrawal), new Timestamp(clock.millis() + Withdrawal.UPDATE_INTERVAL));
    }

    @Test
    public void should_not_withdraw_to_unknown_address() {
        var withdraw = new Withdraw(user.id, new Address(UUID.randomUUID().toString()), BigDecimal.TEN);

        assertThrows(User.UnknownUserAddress.class, () -> subject.handle(withdraw));
    }

    @Test
    public void should_not_allow_to_over_withdraw() {
        var barrier = new CyclicBarrier(10);
        var withdraw = new Withdraw(user.id, address, BigDecimal.ONE);
        ArrayList<Thread> threads = ThreadUtils.spawn(10, () -> {
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            subject.handle(withdraw);
        });

        ThreadUtils.joinAll(threads);

        assertEquals(BigDecimal.ZERO, user.balance().free());
        assertEquals(BigDecimal.TEN, user.balance().frozen());
    }


}