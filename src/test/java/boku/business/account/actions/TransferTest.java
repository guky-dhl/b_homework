package boku.business.account.actions;

import boku.business.account.user.*;
import boku.infra.thread.ThreadUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        subject.handle(transfer);


        assertEquals(BigDecimal.ZERO, sender.balance().free());
        assertEquals(DEFAULT_BALANCE, receiver.balance().free());
    }

    @Test
    public void should_return_error_if_sender_has_insufficient_balance() {
        var transfer = new Transfer(sender.id, receiver.id, DEFAULT_BALANCE.add(BigDecimal.ONE));

        assertThrows(Balance.LowBalance.class, () -> subject.handle(transfer));

    }

    @Test
    public void should_correctly_handle_parallel_requests() {
        var barrier = new CyclicBarrier(10);
        var transfer = new Transfer(sender.id, receiver.id, BigDecimal.TEN);
        ArrayList<Thread> threads = ThreadUtils.spawn(10, () -> {
            try {
                barrier.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            subject.handle(transfer);
        });

        ThreadUtils.joinAll(threads);

        assertEquals(BigDecimal.ZERO, sender.balance().free());
        assertEquals(DEFAULT_BALANCE, receiver.balance().free());
    }
}