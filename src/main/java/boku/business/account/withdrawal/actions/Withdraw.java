package boku.business.account.withdrawal.actions;

import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.user.Users;
import boku.business.account.withdrawal.Withdrawal;
import boku.business.account.withdrawal.WithdrawalService;
import boku.business.account.withdrawal.Withdrawals;
import boku.infra.command.Command;
import boku.infra.job.JobService;
import boku.infra.time.Timestamp;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.time.Clock;


public record Withdraw(User.UserId user,
                       WithdrawalService.Address address,

                       BigDecimal amount) implements Command<Withdraw.Result> {

    public record Result(Withdrawal.WithdrawalId id) {
    }

    public static class Handler implements Command.Handler<Withdraw, Withdraw.Result> {

        private final Users users;
        private final Transactions transactions;
        private final Withdrawals withdrawals;
        private final WithdrawalService withdrawalService;
        private final JobService jobService;
        private final Clock clock;

        @Inject
        public Handler(Users users, Transactions transactions, Withdrawals withdrawals, WithdrawalService withdrawalService, JobService jobService, Clock clock) {
            this.users = users;
            this.transactions = transactions;
            this.withdrawals = withdrawals;
            this.withdrawalService = withdrawalService;
            this.jobService = jobService;
            this.clock = clock;
        }

        @Override
        public Result handle(Withdraw command) {
            var user = users.get(command.user);
            var withdrawal = new Withdrawal(command.address, command.amount, user.id);
            synchronized (user) {
                if (!user.belongs_to_user(command.address)) {
                    throw new User.UnknownUserAddress(user, command.address);
                }
                withdrawals.save(withdrawal);
                withdrawalService.requestWithdrawal(withdrawal.externalId, command.address, command.amount);
                var request_transaction = Transaction.withdrawal(user.id, command.amount, withdrawal.id);
                user.apply(request_transaction, transactions);
                var updateStateCommand = new UpdateWithdrawalState(user, request_transaction, withdrawal);
                jobService.scheduleCommand(updateStateCommand, new Timestamp(clock.millis() + Withdrawal.UPDATE_INTERVAL));
            }

            return new Result(withdrawal.id);
        }

        @Override
        public Class<Withdraw> commandClass() {
            return Withdraw.class;
        }
    }
}
