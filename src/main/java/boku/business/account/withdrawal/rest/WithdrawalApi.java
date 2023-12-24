package boku.business.account.withdrawal.rest;

import boku.business.account.user.User;
import boku.business.account.withdrawal.actions.ListWithdrawals;
import boku.business.account.withdrawal.actions.Withdraw;
import boku.infra.rest.RestUtils;
import io.javalin.Javalin;

import java.util.List;
import java.util.UUID;

public interface WithdrawalApi {
    public static final String PATH =  "/withdrawal";
    static Javalin register(Javalin javalin){
        return javalin.put(PATH, ctx -> {
            var command = ctx.bodyAsClass(Withdraw.class);
            var result = RestUtils.getCommandHandler(ctx).handle(command);
            ctx.json(result);
        }).
        get(PATH + "/list", ctx -> {
            var userId = new User.UserId(UUID.fromString(ctx.queryParam("user_id")));
            var page = Integer.valueOf(ctx.queryParam("page"));
            var perPage = Integer.valueOf(ctx.queryParam("per_page"));
            var command = new ListWithdrawals(userId, page, perPage);
            var result = RestUtils.getCommandHandler(ctx).handle(command);
            ctx.json(result);
        });
    }
}
