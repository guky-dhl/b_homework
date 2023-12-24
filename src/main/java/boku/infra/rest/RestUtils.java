package boku.infra.rest;

import boku.infra.command.CommandHandler;
import io.javalin.Javalin;
import io.javalin.http.Context;

public interface RestUtils {
    String COMMAND_HANDLER = "command_handler";

    static CommandHandler getCommandHandler(Context context) {
        return context.attribute(COMMAND_HANDLER);
    }

    static void registerCommandHandler(Javalin javalin, CommandHandler handler) {
        javalin.before(ctx -> ctx.attribute(COMMAND_HANDLER, handler));
    }
}
