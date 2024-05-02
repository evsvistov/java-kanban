package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(gson);
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (method.equals("GET") && pathParts.length == 2) {
                sendText(exchange, gson.toJson(taskManager.getListOfTasks()), 200);
            } else if (method.equals("GET") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                Task task = taskManager.getTaskId(id);
                sendText(exchange, gson.toJson(task), 200);
            } else if (method.equals("POST") && pathParts.length == 2) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                taskManager.createTask(task);
                sendText(exchange, gson.toJson("Задача добавлена"), 201);
            } else if (method.equals("POST") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                task.setId(id);
                taskManager.updateTask(task);
                sendText(exchange, gson.toJson("Задача обновлена"), 201);
            } else if (method.equals("DELETE") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteTask(id);
                sendText(exchange, gson.toJson("Задача удалена"), 200);
            } else {
                sendText(exchange, gson.toJson("Bad request"), 400);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (Exception e) {
            sendText(exchange, gson.toJson("Internal Server Error"), 500);
        }
    }
}
