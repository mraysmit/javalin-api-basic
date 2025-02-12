import dev.mars.Routes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RoutesTest {

    @Test
    public void testGetUserById() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/users/1");
            assertEquals(404, response.code());
        });
    }

    @Test
    public void testGetAllUsers() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/users");
            assertEquals(200, response.code());
        });
    }

    @Test
    public void testAddUser() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/users", "{\"name\":\"John Doe\"}");
            assertEquals(201, response.code());
        });
    }

    @Test
    public void testUpdateUser() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.put("/users/1", "{\"name\":\"John Doe\"}");
            assertEquals(204, response.code());
        });
    }

    @Test
    public void testDeleteUser() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.delete("/users/1");
            assertEquals(204, response.code());
        });
    }

    @Test
    public void testGetUsersPaginated() {
        Javalin app = Javalin.create();
        Routes.configure(app);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/users/paginated?page=1&size=10");
            assertEquals(200, response.code());
        });
    }
}