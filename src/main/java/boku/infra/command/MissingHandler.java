package boku.infra.command;

public class MissingHandler extends IllegalArgumentException{
    public <R> MissingHandler(Command<R> c){
        super("Command missing handler %s".formatted(c.getClass()));
    }
}
