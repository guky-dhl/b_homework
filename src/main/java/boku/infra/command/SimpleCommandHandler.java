package boku.infra.command;

import java.util.HashMap;

public class SimpleCommandHandler implements CommandHandler {
    private final HashMap<Class<?>, Command.Handler<?, ?>> handlers;

    public SimpleCommandHandler() {
        this.handlers = new HashMap<>();
    }

    public <R, C extends Command<R>> void add(Command.Handler<C, R> handler){
        this.handlers.put(handler.command_class(), handler);
    }

    public <R, C extends Command<R>> R handle(C command){
        @SuppressWarnings("unchecked")
        Command.Handler<C, R> handler = (Command.Handler<C, R>) this.handlers.get(command.getClass());
        if (handler == null) {
            throw new MissingHandler(command);
        }
        return handler.handle(command);
    }
}
