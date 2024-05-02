import com.google.gson.Gson;
import enums.TaskStatus;
import http.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpHistoryTest {
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
    void testGetHistory() throws IOException, InterruptedException {
        // Создаем задачи и добавляем их в историю
        Task task = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, ZonedDateTime.now(), 5);
        manager.createTask(task);
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description 1",
                TaskStatus.NEW, epic.getId(), ZonedDateTime.now().plusHours(1), 30);
        manager.createSubTask(subtask);

        manager.getTaskId(task.getId());
        manager.getEpicId(epic.getId());
        manager.getSubTasksId(subtask.getId());

        // Отправляем GET-запрос на получение истории
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученную историю
        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(3, history.length);
        assertEquals(task.getId(), history[0].getId());
        assertEquals(epic.getId(), history[1].getId());
        assertEquals(subtask.getId(), history[2].getId());


    }

    @Test
    void testGetEmptyHistory() throws IOException, InterruptedException {
        // Отправляем GET-запрос на получение истории, когда она пуста
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что история пуста
        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length);
    }

    @Test
    void testInvalidRequest() throws IOException, InterruptedException {
        // Отправляем некорректный запрос (POST вместо GET)
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(400, response.statusCode());
    }
}
