package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
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
                sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
            } else {
                sendText(exchange, gson.toJson("Bad request"), 400);
            }
        } catch (Exception e) {
            sendText(exchange, gson.toJson("Internal Server Error"), 500);
        }
    }
}

