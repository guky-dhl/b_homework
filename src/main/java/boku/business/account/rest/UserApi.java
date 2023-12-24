package boku.business.account.rest;

import boku.business.account.actions.Transfer;
import boku.infra.rest.RestUtils;
import io.javalin.Javalin;

public interface UserApi {
    String PATH = "/transfer";

    static Javalin register(Javalin javalin) {
        return javalin.put(PATH, ctx -> {
            var command = ctx.bodyAsClass(Transfer.class);
            RestUtils.getCommandHandler(ctx).handle(command);
        });
    }
}
