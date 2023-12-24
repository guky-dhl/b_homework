package boku;

import boku.business.account.actions.Transfer;
import boku.business.account.user.User;
import boku.business.account.withdrawal.WithdrawalService;
import boku.business.account.withdrawal.WithdrawalServiceStub;
import boku.infra.command.Command;
import boku.infra.command.CommandHandler;
import boku.infra.command.SimpleCommandHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.javalin.Javalin;

import java.math.BigDecimal;


public class App {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new AppModule());
        var commandHandler = injector.getInstance(CommandHandler.class);
        commandHandler.handle(new Transfer(new User.UserId(), new User.UserId(), BigDecimal.TEN));

        var app = Javalin.create(/*config*/)
                .before(ctx -> ctx.attribute("command_handler", commandHandler))
                .get("/", ctx -> ctx.result("Hello World"))
                .start(7070);
    }

    public static class AppModule extends AbstractModule {
        public void configure() {
            bind(CommandHandler.class).to(SimpleCommandHandler.class).asEagerSingleton();
            bind(WithdrawalService.class).to(WithdrawalServiceStub.class).asEagerSingleton();
            var commandsBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Command.Handler<?, ?>>() {
            });
            commandsBinder.addBinding().to(Transfer.Handler.class).asEagerSingleton();
        }
    }
}