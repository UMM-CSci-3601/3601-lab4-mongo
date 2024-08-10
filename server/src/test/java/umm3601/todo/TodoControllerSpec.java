package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;

public class TodoControllerSpec {
  private TodoController todoController;

  // The client and database that will be used
  // for all the tests in this spec file.
  private static MongoClient mongoClient;
  private static MongoDatabase db;

  @Mock
  private Context ctx;

  @Captor
  private ArgumentCaptor<ArrayList<Todo>> todoArrayListCaptor;

  @BeforeAll
  static void setupAll() {
    String mongoAddr = System.getenv().getOrDefault("MONGO_ADDR", "localhost");

    mongoClient = MongoClients.create(
        MongoClientSettings.builder()
            .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(mongoAddr))))
            .build());
    db = mongoClient.getDatabase("test");
  }

  @AfterAll
  static void teardown() {
    db.drop();
    mongoClient.close();
  }

  @BeforeEach
  void setupEach() {
    MockitoAnnotations.openMocks(this);

    MongoCollection<Document> todoDocuments = db.getCollection("todos");
    todoDocuments.drop();
    List<Document> testTodos = new ArrayList<>();
    testTodos.add(
        new Document()
            .append("owner", "Chris")
            .append("status", false)
            .append("category", "software design")
            .append("body", "Todo 0"));
    testTodos.add(
        new Document()
            .append("owner", "Pat")
            .append("status", true)
            .append("category", "homework")
            .append("body", "Todo 1"));
    testTodos.add(
        new Document()
            .append("owner", "Jamie")
            .append("status", true)
            .append("category", "homework")
            .append("body", "Todo 2"));
    testTodos.add(
        new Document()
            .append("owner", "Chris")
            .append("status", true)
            .append("category", "software design")
            .append("body", "Todo 3"));
    testTodos.add(
        new Document()
            .append("owner", "Pat")
            .append("status", false)
            .append("category", "video games")
            .append("body", "Todo 4"));

    todoDocuments.insertMany(testTodos);

    todoController = new TodoController(db);
  }

    /**
   * Verify that we can successfully build a UserController
   * and call it's `addRoutes` method. This doesn't verify
   * much beyond that the code actually runs without throwing
   * an exception. We do, however, confirm that the `addRoutes`
   * causes `.get()` to be called at least twice.
   */
  @Test
  public void canBuildController() throws IOException {
    Javalin mockServer = Mockito.mock(Javalin.class);
    todoController.addRoutes(mockServer);

    // Verify that calling `addRoutes()` above caused `get()` to be called
    // on the server at least twice. We use `any()` to say we don't care about
    // the arguments that were passed to `.get()`.
    verify(mockServer, Mockito.atLeast(1)).get(any(), any());
  }

  @Test
  void canGetAllUsers() throws IOException {
    // When something asks the (mocked) context for the queryParamMap,
    // it will return an empty map (since there are no query params in this case
    // where we want all users)
    when(ctx.queryParamMap()).thenReturn(Collections.emptyMap());

    // Now, go ahead and ask the userController to getUsers
    // (which will, indeed, ask the context for its queryParamMap)
    todoController.getTodos(ctx);

    // We are going to capture an argument to a function, and the type of that
    // argument will be
    // of type ArrayList<User> (we said so earlier using a Mockito annotation like
    // this):
    // @Captor
    // private ArgumentCaptor<ArrayList<User>> userArrayListCaptor;
    // We only want to declare that captor once and let the annotation
    // help us accomplish reassignment of the value for the captor
    // We reset the values of our annotated declarations using the command
    // `MockitoAnnotations.openMocks(this);` in our @BeforeEach

    // Specifically, we want to pay attention to the ArrayList<User> that is passed
    // as input
    // when ctx.json is called --- what is the argument that was passed? We capture
    // it and can refer to it later
    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    // Check that the database collection holds the same number of documents as the
    // size of the captured List<User>
    assertEquals(db.getCollection("todos").countDocuments(), todoArrayListCaptor.getValue().size());
  }

  @Test
  void canGetLimitedNumberOfTodos() throws IOException {
    // When something asks the (mocked) context for the queryParamMap,
    // it will return a map with a single key-value pair
    // (since we want to limit the number of users)
    when(ctx.queryParamAsClass("limit", Integer.class))
        .thenReturn(Validator.create(Integer.class, "3", "limit"));

    // when(ctx.queryParamMapAsClass())
    //   .thenReturn(Collections.singletonMap(
    //           "limit",
    //           Arrays.asList(new String[] {"3"})));

    // Now, go ahead and ask the userController to getUsers
    // (which will, indeed, ask the context for its queryParamMap)
    todoController.getTodos(ctx);

    // We are going to capture an argument to a function, and the type of that
    // argument will be
    // of type ArrayList<User> (we said so earlier using a Mockito annotation like
    // this):
    // @Captor
    // private ArgumentCaptor<ArrayList<User>> userArrayListCaptor;
    // We only want to declare that captor once and let the annotation
    // help us accomplish reassignment of the value for the captor
    // We reset the values of our annotated declarations using the command
    // `MockitoAnnotations.openMocks(this);` in our @BeforeEach

    // Specifically, we want to pay attention to the ArrayList<User> that is passed
    // as input
    // when ctx.json is called --- what is the argument that was passed? We capture
    // it and can refer to it later
    verify(ctx).json(todoArrayListCaptor.capture());
    verify(ctx).status(HttpStatus.OK);

    final int limit = 3;
    // Check that the database collection holds the same number of documents as the
    // size of the captured List<User>
    assertEquals(limit, todoArrayListCaptor.getValue().size());
  }
}
