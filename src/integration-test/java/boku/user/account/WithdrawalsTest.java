package boku.user.account;

import boku.App;
import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.user.Users;
import boku.business.account.withdrawal.WithdrawalService;
import boku.business.account.withdrawal.actions.ListWithdrawals;
import boku.business.account.withdrawal.actions.Withdraw;
import boku.business.account.withdrawal.rest.WithdrawalApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WithdrawalsTest {

    final Injector injector = Guice.createInjector(new App.AppModule());
    final User user1 = new User(new User.UserId());
    final WithdrawalService.Address address = new WithdrawalService.Address(UUID.randomUUID().toString());

    Javalin app;
    int port = ThreadLocalRandom.current().nextInt(1000, 10000);
    Gson gson = new GsonBuilder().create();
    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setup() {
        var users = injector.getInstance(Users.class);
        var transactions = injector.getInstance(Transactions.class);
        user1.add_address(address);
        user1.apply(Transaction.deposit(user1.id, BigDecimal.valueOf(1000)), transactions);
        users.save(user1);
        app = App.createApp(injector);
        app.start(port);
    }

    @Test
    public void should_create_and_list_withdrawal() throws IOException, InterruptedException {
        var withdrawal = new Withdraw(user1.id, address, BigDecimal.TEN);
        var body = gson.toJson(withdrawal);

        var request = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(body)).uri(URI.create("http://localhost:%s%s".formatted(port, WithdrawalApi.PATH))).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        var new_withdrawal = gson.fromJson(response, Withdraw.Result.class);

        request = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:%s%s/list?user_id=%s&page=1&per_page=10".formatted(port, WithdrawalApi.PATH, user1.id))).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        var list_withdrawals = gson.fromJson(response, ListWithdrawals.Result.class);
        var listed = list_withdrawals.withdrawals().stream().filter((it) -> it.id().equals(new_withdrawal.id())).findFirst();

        assertTrue(listed.isPresent());
    }

    @AfterEach
    void stop() {
        app.stop();
    }

}
