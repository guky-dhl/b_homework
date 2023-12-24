package boku.business.account.withdrawal;

import boku.business.account.user.User;
import boku.infra.persistance.BasicRepository;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Withdrawals extends BasicRepository<Withdrawal.WithdrawalId, Withdrawal> {
    private final ConcurrentHashMap<User.UserId, List<Withdrawal>> withdrawalsByUser = new ConcurrentHashMap<>();

    public List<Withdrawal> byUserId(User.UserId userId, int skip, int count) {
        return this.getUserWithdrawals(userId).stream().skip(skip).limit(count).toList();
    }

    public int countByUserId(User.UserId userId) {
        return getUserWithdrawals(userId).size();
    }

    private List<Withdrawal> getUserWithdrawals(User.UserId userId) {
        return withdrawalsByUser.computeIfAbsent(userId, (id) -> new ArrayList<>());
    }

    @Override
    public void save(Withdrawal withdrawal) {
        super.save(withdrawal);
        var user_withdrawals = getUserWithdrawals(withdrawal.userId);
        user_withdrawals.add(withdrawal);
    }
}
