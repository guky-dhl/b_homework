package boku.business.account.withdrawal.rest;

import boku.business.account.user.User;
import boku.business.account.withdrawal.actions.ListWithdrawals;
import boku.business.account.withdrawal.actions.Withdraw;
import boku.infra.rest.RestUtils;
import io.javalin.Javalin;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawalApi {
    String PATH = "/withdrawal";

    static Javalin register(Javalin javalin) {
        return javalin.put(PATH, ctx -> {
                    var command = ctx.bodyAsClass(Withdraw.class);
                    var result = RestUtils.getCommandHandler(ctx).handle(command);
                    ctx.json(result);
                }).
                get(PATH + "/list", ctx -> {
                    if (ctx.queryParam("user_id") == null) {
                        throw new IllegalArgumentException("User id parameter is missing");
                    }

                    var userId = new User.UserId(UUID.fromString(ctx.queryParam("user_id")));
                    var page = Optional.ofNullable(ctx.queryParam("page")).map(Integer::parseInt).orElse(1);
                    var perPage = Optional.ofNullable(ctx.queryParam("per_page")).map(Integer::parseInt).orElse(10);
                    var command = new ListWithdrawals(userId, page, perPage);
                    var result = RestUtils.getCommandHandler(ctx).handle(command);
                    ctx.json(result);
                });
    }
}
