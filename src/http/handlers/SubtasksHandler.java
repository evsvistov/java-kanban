package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import task.SubTask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
                sendText(exchange, gson.toJson(taskManager.getListOfSubTasks()), 200);
            } else if (method.equals("GET") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                SubTask subtask = taskManager.getSubTasksId(id);
                sendText(exchange, gson.toJson(subtask), 200);
            } else if (method.equals("POST") && pathParts.length == 2) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                SubTask subtask = gson.fromJson(body, SubTask.class);
                taskManager.createSubTask(subtask);
                sendText(exchange, gson.toJson("Подзадача добавлена"), 201);
            } else if (method.equals("POST") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                SubTask subtask = gson.fromJson(body, SubTask.class);
                subtask.setId(id);
                taskManager.updateSubTask(subtask);
                sendText(exchange, gson.toJson("Подзадача обновлена"), 201);
            } else if (method.equals("DELETE") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteSubTask(id);
                sendText(exchange, gson.toJson("Подзадача удалена"), 200);
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
