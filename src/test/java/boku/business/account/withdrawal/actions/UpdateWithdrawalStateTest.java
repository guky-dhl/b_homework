package boku.business.account.withdrawal.actions;

import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.withdrawal.Withdrawal;
import boku.business.account.withdrawal.WithdrawalService;
import boku.infra.job.JobService;
import boku.infra.time.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static boku.business.account.withdrawal.WithdrawalService.WithdrawalState.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UpdateWithdrawalStateTest {

    public static final BigDecimal WITHDRAWAL_AMOUNT = BigDecimal.TEN;
    private final WithdrawalService.Address address = new WithdrawalService.Address("abc");
    private final User user = new User();
    private final Withdrawal withdrawal = new Withdrawal(address, BigDecimal.TEN, user.id);
    private final Transaction request_withdrawal_transaction = Transaction.withdrawal(user.id, WITHDRAWAL_AMOUNT, withdrawal.id);
    private final WithdrawalService withdrawalService = mock(WithdrawalService.class);
    private final Transactions transactions = new Transactions();
    private final JobService jobService = mock(JobService.class);
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final UpdateWithdrawalState.Handler subject = new UpdateWithdrawalState.Handler(withdrawalService, transactions, jobService, clock);


    @BeforeEach
    void setUp() {
        user.apply(Transaction.deposit(user.id, BigDecimal.TEN), transactions);
        user.apply(request_withdrawal_transaction, transactions);
    }

    @Test
    public void should_complete_transaction_when_ready() {
        var update_status = new UpdateWithdrawalState(user, request_withdrawal_transaction, withdrawal);
        when(withdrawalService.getRequestState(withdrawal.externalId)).thenReturn(COMPLETED);

        subject.handle(update_status);

        assertEquals(withdrawal.state(), COMPLETED);
        assertEquals(withdrawal.completedAt(), new Timestamp(clock.millis()));
        assertEquals(user.balance().free(), BigDecimal.ZERO);
        assertEquals(user.balance().frozen(), BigDecimal.ZERO);

        var transaction = transactions.all().stream().filter((it) -> it.type == Transaction.Type.WITHDRAWAL_DONE).findFirst().get();
        assertEquals(request_withdrawal_transaction.withdrawalId, transaction.withdrawalId);
        assertEquals(request_withdrawal_transaction.amount, WITHDRAWAL_AMOUNT);
    }

    @Test
    public void should_fail_transaction_when_ready() {
        var update_status = new UpdateWithdrawalState(user, request_withdrawal_transaction, withdrawal);
        when(withdrawalService.getRequestState(withdrawal.externalId)).thenReturn(FAILED);

        subject.handle(update_status);

        assertEquals(withdrawal.state(), FAILED);
        assertEquals(withdrawal.completedAt(), new Timestamp(clock.millis()));
        assertEquals(user.balance().free(), WITHDRAWAL_AMOUNT);
        assertEquals(user.balance().frozen(), BigDecimal.ZERO);

        var transaction = transactions.all().stream().filter((it) -> it.type == Transaction.Type.WITHDRAWAL_FAILED).findFirst().get();
        assertEquals(request_withdrawal_transaction.withdrawalId, transaction.withdrawalId);
        assertEquals(request_withdrawal_transaction.amount, WITHDRAWAL_AMOUNT);
    }

    @Test
    public void should_reschedule_update_still_processing() {
        var update_status = new UpdateWithdrawalState(user, request_withdrawal_transaction, withdrawal);
        when(withdrawalService.getRequestState(withdrawal.externalId)).thenReturn(PROCESSING);

        subject.handle(update_status);

        assertEquals(withdrawal.state(), PROCESSING);
        assertEquals(withdrawal.completedAt(), Timestamp.zero());
        assertEquals(user.balance().free(), BigDecimal.ZERO);
        assertEquals(user.balance().frozen(), WITHDRAWAL_AMOUNT);

        verify(jobService).scheduleCommand(update_status, new Timestamp(clock.millis() + Withdrawal.UPDATE_INTERVAL));
    }
}