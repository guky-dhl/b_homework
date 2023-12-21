package boku.infra;

public interface Command<Response> {

    interface Handler<C extends Command<R>, R> {
        R handle(C command);
        Class<C> command_class();
    }
}

