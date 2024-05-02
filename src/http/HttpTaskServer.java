package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;

import com.sun.net.httpserver.HttpServer;
import enums.TaskStatus;
import http.adapter.DurationAdapter;
import http.adapter.TaskStatusAdapter;
import http.adapter.ZonedDateTimeAdapter;
import http.handlers.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.ZonedDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
                .create();

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
    }

    public Gson getGson() {
        return this.gson;
    }
}
