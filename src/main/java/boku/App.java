package boku;

import boku.business.account.actions.Transfer;
import boku.business.account.rest.UserApi;
import boku.business.account.withdrawal.WithdrawalService;
import boku.business.account.withdrawal.WithdrawalServiceStub;
import boku.business.account.withdrawal.actions.ListWithdrawals;
import boku.business.account.withdrawal.actions.UpdateWithdrawalState;
import boku.business.account.withdrawal.actions.Withdraw;
import boku.business.account.withdrawal.rest.WithdrawalApi;
import boku.infra.command.Command;
import boku.infra.command.CommandHandler;
import boku.infra.command.SimpleCommandHandler;
import boku.infra.job.JobService;
import boku.infra.job.SimpleJobService;
import boku.infra.rest.RestUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Clock;


public class App {
    public static void main(String[] args) {
        createApp(Guice.createInjector(new AppModule())).start(7070);
    }

    public static Javalin createApp(Injector injector) {
        var commandHandler = injector.getInstance(CommandHandler.class);

        var app = Javalin.create(App::initGson);
        RestUtils.registerCommandHandler(app, commandHandler);

        WithdrawalApi.register(app);
        UserApi.register(app);


        return app;
    }

    public static void initGson(JavalinConfig config) {
        Gson gson = new GsonBuilder().create();
        JsonMapper gsonMapper = new JsonMapper() {

            @NotNull
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }

            @NotNull
            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };
        config.jsonMapper(gsonMapper);
    }

    public static class AppModule extends AbstractModule {
        public void configure() {
            bind(CommandHandler.class).to(SimpleCommandHandler.class).asEagerSingleton();
            bind(WithdrawalService.class).to(WithdrawalServiceStub.class).asEagerSingleton();
            bind(JobService.class).to(SimpleJobService.class).asEagerSingleton();
            bind(java.time.Clock.class).toInstance(Clock.systemUTC());
            var commandsBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Command.Handler<?, ?>>() {
            });
            commandsBinder.addBinding().to(Withdraw.Handler.class).asEagerSingleton();
            commandsBinder.addBinding().to(UpdateWithdrawalState.Handler.class).asEagerSingleton();
            commandsBinder.addBinding().to(ListWithdrawals.Handler.class).asEagerSingleton();
            commandsBinder.addBinding().to(Transfer.Handler.class).asEagerSingleton();
        }
    }
}