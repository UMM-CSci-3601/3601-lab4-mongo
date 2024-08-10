package umm3601.todo;

import java.util.ArrayList;

import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;

import com.mongodb.client.MongoDatabase;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validator;
import umm3601.Controller;

public class TodoController implements Controller {
  private static final String API_TODOS = "/api/todos";

  private final JacksonMongoCollection<Todo> todoCollection;

  public TodoController(MongoDatabase db) {
    todoCollection = JacksonMongoCollection
      .builder()
      .build(
        db,
        "todos",
        Todo.class,
        UuidRepresentation.STANDARD);
  }

  public void addRoutes(Javalin server) {
    // List users, filtered using query parameters
    server.get(API_TODOS, this::getTodos);
  }

  public void getTodos(Context ctx) {
    int limit = getLimit(ctx);

    ArrayList<Todo> todos = todoCollection
        .find()
        .limit(limit)
        .into(new ArrayList<>());

    ctx.json(todos);
    ctx.status(HttpStatus.OK);
  }

  private int getLimit(Context ctx) {
    int limit = 0;
    Validator<Integer> validator = ctx.queryParamAsClass("limit", Integer.class);
    if (validator != null) {
      limit = validator
        .check(it -> it >= 0, "Limit size should be non-negative")
        .get();
    }
    return limit;
  }
}
