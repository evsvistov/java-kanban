import com.google.gson.Gson;
import enums.TaskStatus;
import http.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPrioritizedTasks {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        manager.deletingAllTasks();
        manager.deletingAllsubTasks();
        manager.deletingAllsubTasks();
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Создаем задачи с разными приоритетами
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, ZonedDateTime.now(), 60);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.NEW, ZonedDateTime.now().plusHours(1), 30);
        manager.createTask(task1);
        manager.createTask(task2);

        // Отправляем GET-запрос на получение списка приоритизированных задач
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученный список приоритизированных задач
        assertEquals(200, response.statusCode());
        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritizedTasks.length);

    }

    @Test
    void testGetPrioritizedTasksWithEmptyList() throws IOException, InterruptedException {
        // Отправляем GET-запрос на получение списка приоритизированных задач, когда список пуст
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что список приоритизированных задач пуст
        assertEquals(200, response.statusCode());
        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, prioritizedTasks.length);
    }

    @Test
    void testInvalidRequest() throws IOException, InterruptedException {
        // Отправляем некорректный запрос (POST вместо GET)
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(400, response.statusCode());
    }
}
