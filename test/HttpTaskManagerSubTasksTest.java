import com.google.gson.Gson;
import enums.TaskStatus;
import exceptions.NotFoundException;
import http.HttpTaskServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import manager.*;
import task.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubTasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    public HttpTaskManagerSubTasksTest() throws IOException {
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
    void testGetSubtasks() throws IOException, InterruptedException {
        // Создаем эпик и подзадачи
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);
        SubTask subtask1 = new SubTask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 30);
        SubTask subtask2 = new SubTask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS, epic.getId(), ZonedDateTime.now().plusHours(2), 60);
        manager.createSubTask(subtask1);
        manager.createSubTask(subtask2);

        // Отправляем GET-запрос на получение списка подзадач
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученный список подзадач
        assertEquals(200, response.statusCode());
        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);
        assertEquals(2, subtasks.length);
        // Дополнительные проверки полей подзадач
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 30);
        manager.createSubTask(subtask);

        // Отправляем GET-запрос на получение подзадачи по идентификатору
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученную подзадачу
        assertEquals(200, response.statusCode());
        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(subtask.getId(), retrievedTask.getId());
        assertEquals(subtask.getName(), retrievedTask.getName());
        assertEquals(subtask.getDescription(), retrievedTask.getDescription());
        assertEquals(subtask.getStatus(), retrievedTask.getStatus());
        assertEquals(subtask.getDuration(), retrievedTask.getDuration());

        assertEquals(subtask.getStartTime().format(Managers.formatter), retrievedTask.getStartTime().format(Managers.formatter));
        assertEquals(subtask.getEndTime().format(Managers.formatter), retrievedTask.getEndTime().format(Managers.formatter));


        //assertEquals(200, response.statusCode());
        //SubTask retrievedSubtask = gson.fromJson(response.body(), SubTask.class);
        //assertEquals(subtask, retrievedSubtask);
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        // Создаем подзадачу
        SubTask subtask = new SubTask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 30);
        String subtaskJson = gson.toJson(subtask);

        // Отправляем POST-запрос на создание подзадачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что подзадача добавлена в менеджер
        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getListOfSubTasks().size());
        // Дополнительные проверки полей подзадачи
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 30);
        manager.createSubTask(subtask);

        // Обновляем подзадачу
        SubTask updatedSubtask = new SubTask("Updated Subtask 1", "Updated Description 1", TaskStatus.IN_PROGRESS, epic.getId(), ZonedDateTime.now(), 60);
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        // Отправляем POST-запрос на обновление подзадачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что подзадача обновлена в менеджере
        assertEquals(201, response.statusCode());
        SubTask retrievedSubtask = manager.getSubTasksId(subtask.getId());
        assertEquals(updatedSubtask.getName(), retrievedSubtask.getName());
        assertEquals(updatedSubtask.getDescription(), retrievedSubtask.getDescription());
        assertEquals(updatedSubtask.getStatus(), retrievedSubtask.getStatus());
        assertEquals(updatedSubtask.getDuration(), retrievedSubtask.getDuration());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        // Создаем эпик и подзадачу
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description 1", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 30);
        manager.createSubTask(subtask);

        // Отправляем DELETE-запрос на удаление подзадачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Проверяем код ответа и что подзадача удалена из менеджера
        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getSubTasksId(subtask.getId()));
    }

    @Test
    void testGetNonExistentSubtask() throws IOException, InterruptedException {
        // Отправляем GET-запрос на получение несуществующей подзадачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Проверяем код ответа
        assertEquals(404, response.statusCode());
    }


}
