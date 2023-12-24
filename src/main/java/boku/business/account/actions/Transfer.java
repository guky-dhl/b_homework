package boku.business.account.actions;

import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.user.Users;
import boku.infra.command.Command;
import boku.infra.command.Voidy;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;


public record Transfer(User.UserId sender, User.UserId receiver,
                       BigDecimal amount) implements Command<Voidy> {


    @Singleton
    public static class Handler implements Command.Handler<Transfer, Voidy> {

        private final Users users;
        private final Transactions transactions;

        @Inject
        public Handler(Users users, Transactions transactions) {
            this.users = users;
            this.transactions = transactions;
        }

        @Override
        public Voidy handle(Transfer command) {
            var sender = users.get(command.sender);
            var receiver = users.get(command.receiver);

            // Potential deadlock place. If something trys to lock same users in different order. Could be solved by taking lock with timeout, but it will slow down performance.
            // In real life scenarios usually solved database transactions, but deadlocks there are still possible.
            synchronized (sender) {
                synchronized (receiver) {
                    sender.apply(Transaction.transfer(command.sender, command.amount.negate()), transactions);
                    receiver.apply(Transaction.transfer(command.receiver, command.amount), transactions);
                }
            }

            return Voidy.INSTANCE;
        }

        @Override
        public Class<Transfer> commandClass() {
            return Transfer.class;
        }
    }
}
