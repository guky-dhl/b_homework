package boku.user.account;

import boku.App;
import boku.business.account.actions.Transfer;
import boku.business.account.rest.UserApi;
import boku.business.account.user.Transaction;
import boku.business.account.user.Transactions;
import boku.business.account.user.User;
import boku.business.account.user.Users;
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
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferTest {
    final BigDecimal user_balance = BigDecimal.valueOf(1000);
    final Injector injector = Guice.createInjector(new App.AppModule());

    final Users users = injector.getInstance(Users.class);
    final User user1 = new User(new User.UserId());
    final User user2 = new User(new User.UserId());
    Javalin app;
    int port = ThreadLocalRandom.current().nextInt(1000, 10000);
    Gson gson = new GsonBuilder().create();
    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void setup() {
        var transactions = injector.getInstance(Transactions.class);
        user1.apply(Transaction.deposit(user1.id, user_balance), transactions);
        users.save(user1);
        users.save(user2);
        app = App.createApp(injector);
        app.start(port);
    }

    @Test
    public void should_transfer_from_user1_to_user2() throws IOException, InterruptedException {
        var transfer_amount = BigDecimal.TEN;
        var transfer = new Transfer(user1.id, user2.id, BigDecimal.TEN);
        var body = gson.toJson(transfer);

        var request = HttpRequest.newBuilder().PUT(HttpRequest.BodyPublishers.ofString(body)).uri(URI.create("http://localhost:%s%s".formatted(port, UserApi.PATH))).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode());
        var updated_user1 = users.get(user1.id);
        assertEquals(user_balance.subtract(transfer_amount), updated_user1.balance().free());
        var updated_user2 = users.get(user2.id);
        assertEquals(transfer_amount, updated_user2.balance().free());
    }

    @AfterEach
    void stop() {
        app.stop();
    }

}
