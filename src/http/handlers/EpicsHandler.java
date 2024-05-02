package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.NotFoundException;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
                sendText(exchange, gson.toJson(taskManager.getListOfEpics()), 200);
            } else if (method.equals("GET") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                Epic epic = taskManager.getEpicId(id);
                sendText(exchange, gson.toJson(epic), 200);
            } else if (method.equals("POST") && pathParts.length == 2) {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                taskManager.createEpic(epic);
                sendText(exchange, gson.toJson("Эпик добавлен"), 201);
            } else if (method.equals("POST") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                epic.setId(id);
                taskManager.updateEpic(epic);
                sendText(exchange, gson.toJson("Эпик обновлен"), 201);
            } else if (method.equals("DELETE") && pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                taskManager.deleteEpic(id);
                sendText(exchange, gson.toJson("Эпик удален"), 200);
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
