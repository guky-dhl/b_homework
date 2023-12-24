package boku.business.account.withdrawal.actions;

import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.withdrawal.Withdrawal;
import boku.business.account.withdrawal.WithdrawalService;
import boku.infra.command.Command;
import boku.infra.command.Voidy;
import boku.infra.job.JobService;
import boku.infra.time.Timestamp;
import com.google.inject.Inject;

import java.time.Clock;

import static boku.business.account.withdrawal.WithdrawalService.WithdrawalState.COMPLETED;


public record UpdateWithdrawalState(User user,
                                    Transaction request_transaction,
                                    Withdrawal withdrawal) implements Command<Voidy> {
    public static class Handler implements Command.Handler<UpdateWithdrawalState, Voidy> {


        private final WithdrawalService withdrawalService;

        private final Transactions transactions;
        private final JobService jobService;
        private final Clock clock;

        @Inject
        public Handler(WithdrawalService withdrawalService, Transactions transactions, JobService jobService, Clock clock) {
            this.withdrawalService = withdrawalService;
            this.transactions = transactions;
            this.clock = clock;
            this.jobService = jobService;
        }

        @Override
        public Voidy handle(UpdateWithdrawalState command) {
            var state = withdrawalService.getRequestState(command.withdrawal.externalId);
            var now = clock.millis();

            switch (state) {
                case PROCESSING -> jobService.scheduleCommand(command, new Timestamp(now + Withdrawal.UPDATE_INTERVAL));
                case COMPLETED, FAILED -> {
                    synchronized (command.withdrawal) {
                        synchronized (command.user) {
                            command.withdrawal.complete(state, new Timestamp(now));
                            var withdrawal_transaction = state == COMPLETED ? command.request_transaction().complete_withdrawal() : command.request_transaction().fail_withdrawal();
                            command.user.apply(withdrawal_transaction, transactions);
                        }
                    }
                }
            }

            return Voidy.INSTANCE;
        }

        @Override
        public Class<UpdateWithdrawalState> commandClass() {
            return UpdateWithdrawalState.class;
        }
    }
}
