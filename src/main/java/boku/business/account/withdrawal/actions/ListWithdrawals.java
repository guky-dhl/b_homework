package boku.business.account.withdrawal.actions;

import boku.business.account.user.User;
import boku.business.account.withdrawal.Withdrawal.WithdrawalId;
import boku.business.account.withdrawal.WithdrawalService.Address;
import boku.business.account.withdrawal.WithdrawalService.WithdrawalState;
import boku.business.account.withdrawal.Withdrawals;
import boku.infra.command.Command;
import boku.infra.time.Timestamp;
import com.google.inject.Inject;

import java.util.List;

public record ListWithdrawals(User.UserId userId, int page, int perPage) implements Command<ListWithdrawals.Result> {

    public record WithdrawalRecord(WithdrawalId id, Address address, WithdrawalState state, Timestamp timestamp) {
    }

    public record Result(List<WithdrawalRecord> withdrawals, int total, int page) {
    }

    public final static class Handler implements Command.Handler<ListWithdrawals, Result> {

        private final Withdrawals withdrawals;

        @Inject
        public Handler(Withdrawals withdrawals) {
            this.withdrawals = withdrawals;
        }

        @Override
        public Result handle(ListWithdrawals command) {
            var total = withdrawals.countByUserId(command.userId);
            var last = (command.page - 1) * command.perPage;
            var result = withdrawals.byUserId(command.userId, last, command.perPage)
                    .stream()
                    .map((it) -> new WithdrawalRecord(it.id, it.address, it.state(), it.completedAt()))
                    .toList();
            return new Result(result, total, command.page);
        }

        @Override
        public Class<ListWithdrawals> commandClass() {
            return ListWithdrawals.class;
        }
    }

}
