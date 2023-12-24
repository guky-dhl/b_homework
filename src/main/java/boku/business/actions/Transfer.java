package boku.business.actions;

import boku.business.user.Transaction;
import boku.business.user.Transactions;
import boku.business.user.User;
import boku.business.user.Users;
import boku.infra.command.Command;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.ArrayList;


public record Transfer(User.UserId sender, User.UserId receiver,
                       BigDecimal amount) implements Command<Transfer.TransferResult> {


    public record TransferResult(ArrayList<String> errors) {
    }

    @Singleton
    public static class Handler implements Command.Handler<Transfer, Transfer.TransferResult> {

        private final Users users;
        private final Transactions transactions;

        @Inject
        public Handler(Users users, Transactions transactions) {
            this.users = users;
            this.transactions = transactions;
        }

        @Override
        public TransferResult handle(Transfer command) {
            var results = new ArrayList<String>();
            var optionalSender = users.get(command.sender);
            if (optionalSender.isEmpty()) {
                results.add("Sender [%s] does not exists".formatted(command.sender));
            }
            var optionalReceiver = users.get(command.receiver);
            if (optionalReceiver.isEmpty()) {
                results.add("Receiver [%s] does not exists".formatted(command.receiver));
            }

            if (!results.isEmpty()) {
                return new TransferResult(results);
            }

            var sender = optionalSender.get();
            var receiver = optionalReceiver.get();

            // Potential deadlock place. If something trys to lock same users in different order. Could be solved by taking lock with timeout, but it will slow down performance.
            // In real life scenarios usually solved database transactions, but dead locks there are still possible.
            synchronized (sender) {
                synchronized (receiver) {
                    if (!sender.balance().is_sufficient(command.amount)) {
                        results.add("Sender balance is not sufficient tried to transfer [%s], but got [%s]".formatted(command.amount, sender.balance().free()));
                    } else {
                        sender.apply(Transaction.transfer(command.sender, command.amount.negate()), transactions);
                        receiver.apply(Transaction.transfer(command.receiver, command.amount), transactions);
                    }
                }
            }

            return new TransferResult(results);
        }

        @Override
        public Class<Transfer> command_class() {
            return Transfer.class;
        }
    }
}
