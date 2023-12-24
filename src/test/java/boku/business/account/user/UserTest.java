package boku.business.account.user;

import boku.business.account.withdrawal.Withdrawal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private final Transactions transactions = new Transactions();

    @Test
    void should_be_equals_two_users_with_same_id() {
        var user1 = new User(new User.UserId());
        var user2 = new User(user1.id);

        assertEquals(user1, user2);
    }

    @Test
    void should_deposit() {
        var depositAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, depositAmount);

        subject.apply(deposit, transactions);

        assertEquals(subject.balance(), new Balance(depositAmount, BigDecimal.ZERO));
    }

    @Test
    void should_add_to_balance_transfers_to_user() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var transfer = Transaction.transfer(subject.id, transferAmount);

        subject.apply(transfer, transactions);

        assertEquals(subject.balance(), new Balance(transferAmount, BigDecimal.ZERO));
    }

    @Test
    void should_remove_from_balance_when_transfers_from_user() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, transferAmount);
        var transfer = Transaction.transfer(subject.id, transferAmount.negate());
        subject.apply(deposit, transactions);

        subject.apply(transfer, transactions);

        assertEquals(subject.balance(), new Balance(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void should_free_from_balance_when_request_withdrawal() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, transferAmount);
        var withdrawal = Transaction.withdrawal(subject.id, transferAmount, new Withdrawal.WithdrawalId());
        subject.apply(deposit, transactions);

        subject.apply(withdrawal, transactions);

        assertEquals(subject.balance(), new Balance(BigDecimal.ZERO, transferAmount));
    }

    @Test
    void should_free_from_balance_when_withdrawal_completed() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, transferAmount);
        var withdrawal = Transaction.withdrawal(subject.id, transferAmount, new Withdrawal.WithdrawalId());
        subject.apply(deposit, transactions);
        subject.apply(withdrawal, transactions);

        subject.apply(withdrawal.complete_withdrawal(), transactions);

        assertEquals(subject.balance(), new Balance(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void should_return_balance_when_withdrawal_failed() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, transferAmount);
        var withdrawal = Transaction.withdrawal(subject.id, transferAmount, new Withdrawal.WithdrawalId());
        subject.apply(deposit, transactions);
        subject.apply(withdrawal, transactions);

        subject.apply(withdrawal.fail_withdrawal(), transactions);

        assertEquals(subject.balance(), new Balance(transferAmount, BigDecimal.ZERO));
    }

    @Test
    void should_not_accept_other_users_transaction() {
        var transferAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(new User.UserId(), transferAmount);

        assertThrows(User.UnrelatedTransaction.class, () -> subject.apply(deposit, transactions));

    }

    @Test
    void should_store_transaction() {
        var depositAmount = BigDecimal.TEN;
        var subject = new User(new User.UserId());
        var deposit = Transaction.deposit(subject.id, depositAmount);

        subject.apply(deposit, transactions);

        assertTrue(transactions.find(deposit.id).isPresent());
    }

}