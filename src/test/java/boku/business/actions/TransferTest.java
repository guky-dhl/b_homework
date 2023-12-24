package boku.business.actions;

import boku.business.user.Transaction;
import boku.business.user.Transactions;
import boku.business.user.User;
import boku.business.user.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferTest {
    static BigDecimal DEFAULT_BALANCE = BigDecimal.valueOf(100);
    User sender = new User(new User.UserId());
    User receiver = new User(new User.UserId());
    Users users = new Users();
    Transactions transactions = new Transactions();
    Transfer.Handler subject = new Transfer.Handler(users, transactions);

    @BeforeEach
    void setup() {
        users.save(sender);
        sender.apply(Transaction.deposit(sender.id, DEFAULT_BALANCE), transactions);
        users.save(receiver);
    }

    @Test
    public void should_transfer() {
        var transfer = new Transfer(sender.id, receiver.id, DEFAULT_BALANCE);

        var result = subject.handle(transfer);

        assertTrue(result.errors().isEmpty());
        assertEquals(BigDecimal.ZERO, sender.balance().free());
        assertEquals(DEFAULT_BALANCE, receiver.balance().free());
    }

    @Test
    public void should_return_error_if_sender_does_not_exists() {
        var transfer = new Transfer(new User.UserId(), receiver.id, DEFAULT_BALANCE);

        var result = subject.handle(transfer);

        assertEquals(1, result.errors().size());
        var error = result.errors().get(0);
        assertTrue(error.contains("does not exists"));
    }

    @Test
    public void should_return_error_if_receiver_does_not_exists() {
        var transfer = new Transfer(sender.id, new User.UserId(), DEFAULT_BALANCE);

        var result = subject.handle(transfer);

        assertEquals(1, result.errors().size());
        var error = result.errors().get(0);
        assertTrue(error.contains("does not exists"));
    }

    @Test
    public void should_return_error_if_sender_has_insufficient_balance() {
        var transfer = new Transfer(sender.id, receiver.id, DEFAULT_BALANCE.add(BigDecimal.ONE));

        var result = subject.handle(transfer);

        assertEquals(1, result.errors().size());
        var error = result.errors().get(0);
        assertTrue(error.contains("is not sufficient"));
    }

    @Test
    public void should_correctly_handle_parallel_requests() throws Exception {
        var barrier = new CyclicBarrier(10);
        var transfer = new Transfer(sender.id, receiver.id, BigDecimal.TEN);
        ArrayList<Thread> threads = new ArrayList<>();

        for(int i =0; i<10; i++){
            var thread = new Thread(() -> {
                try {
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                subject.handle(transfer);
            });
            thread.start();
            threads.add(thread);
        }

        threads.forEach((it) -> {
            try {
                it.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(BigDecimal.ZERO, sender.balance().free());
        assertEquals(DEFAULT_BALANCE, receiver.balance().free());

    }
}