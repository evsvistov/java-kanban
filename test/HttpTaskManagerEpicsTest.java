import com.google.gson.Gson;
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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testGetEpics() throws IOException, InterruptedException {
        // Создаем эпики
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        // Отправляем GET-запрос на получение списка эпиков
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученный список эпиков
        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
        // Дополнительные проверки полей эпиков
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        // Отправляем GET-запрос на получение эпика по идентификатору
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и полученный эпик
        assertEquals(200, response.statusCode());
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic, retrievedEpic);
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description 1");
        String epicJson = gson.toJson(epic);

        // Отправляем POST-запрос на создание эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что эпик добавлен в менеджер
        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getListOfEpics().size());
        // Дополнительные проверки полей эпика
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        // Обновляем эпик
        Epic updatedEpic = new Epic("Updated Epic 1", "Updated Description 1");
        String updatedEpicJson = gson.toJson(updatedEpic);

        // Отправляем POST-запрос на обновление эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что эпик обновлен в менеджере
        assertEquals(201, response.statusCode());
        Epic retrievedEpic = manager.getEpicId(epic.getId());
        assertEquals(updatedEpic.getName(), retrievedEpic.getName());
        assertEquals(updatedEpic.getDescription(), retrievedEpic.getDescription());
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Epic 1", "Description 1");
        manager.createEpic(epic);

        // Отправляем DELETE-запрос на удаление эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа и что эпик удален из менеджера
        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> manager.getEpicId(epic.getId()));
    }

    @Test
    void testGetNonExistentEpic() throws IOException, InterruptedException {
        // Отправляем GET-запрос на получение несуществующего эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(404, response.statusCode());
    }
}
