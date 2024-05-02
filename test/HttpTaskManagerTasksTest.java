import com.google.gson.Gson;
import enums.TaskStatus;
import exceptions.NotFoundException;
import http.HttpTaskServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import manager.*;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        manager.deletingAllTasks();
        manager.deletingAllsubTasks();
        manager.deletingAllsubTasks();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
     void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, ZonedDateTime.now(), 5);
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);
        //System.out.println("Task JSON: " + taskJson);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        int statusCode = response.statusCode();
        //System.out.println("Response Status Code: " + statusCode);
        assertEquals(201, statusCode);

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getListOfTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");

        Task addedTask = tasksFromManager.getFirst();
        //System.out.println("Added Task: " + gson.toJson(addedTask));
        assertEquals("Test 2", addedTask.getName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTasks() throws IOException, InterruptedException {
        // Создаем задачи
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW, ZonedDateTime.now(), 30);
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS, ZonedDateTime.now().plusHours(1), 60);
        manager.createTask(task1);
        manager.createTask(task2);

        // Отправляем GET-запрос на получение списка задач
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученный список задач
        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
        // Дополнительные проверки полей задач
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW, ZonedDateTime.now(), 60);
        manager.createTask(task);

        // Отправляем GET-запрос на получение задачи по идентификатору
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), retrievedTask.getId());
        assertEquals(task.getName(), retrievedTask.getName());
        assertEquals(task.getDescription(), retrievedTask.getDescription());
        assertEquals(task.getStatus(), retrievedTask.getStatus());
        assertEquals(task.getDuration(), retrievedTask.getDuration());

        assertEquals(task.getStartTime().format(Managers.formatter), retrievedTask.getStartTime().format(Managers.formatter));
        assertEquals(task.getEndTime().format(Managers.formatter), retrievedTask.getEndTime().format(Managers.formatter));
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        // Создаем задачу
        Task task = new Task("Task 1", "Description 1", TaskStatus.NEW, ZonedDateTime.now(), 60);
        manager.createTask(task);

        // Отправляем DELETE-запрос на удаление задачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что задача удалена из менеджера
        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getSubTasksId(task.getId()));
    }
}
