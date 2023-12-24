package boku.infra.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SimpleCommandHandler implements CommandHandler {
    private final Map<Class<?>, Command.Handler<?, ?>> handlers;

    public SimpleCommandHandler() {
        this.handlers = new HashMap<>();
    }

    @Inject
    public SimpleCommandHandler(Set<Command.Handler<?, ?>> commands) {
        this.handlers = commands.stream().collect(Collectors.toMap(Command.Handler::commandClass, (it) -> it));
    }


    public <R, C extends Command<R>> void add(Command.Handler<C, R> handler) {
        this.handlers.put(handler.commandClass(), handler);
    }

    public <R, C extends Command<R>> R handle(C command) {
        @SuppressWarnings("unchecked")
        Command.Handler<C, R> handler = (Command.Handler<C, R>) this.handlers.get(command.getClass());
        if (handler == null) {
            throw new MissingHandler(command);
        }
        return handler.handle(command);
    }
}
