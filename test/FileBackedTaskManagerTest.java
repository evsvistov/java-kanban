import enums.TaskStatus;
import exceptions.ManagerLoadException;
import manager.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager>{

    File tempFileImport;

    @BeforeEach
    public void setUp() {
        super.setUp();

        String resourceDirectoryPath = "test/resources";
        File tempFileExport = new File(resourceDirectoryPath, "export.csv");
        tempFileImport = new File(resourceDirectoryPath, "import.csv");
        try {
            Files.copy(tempFileExport.toPath(), tempFileImport.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    public void tearDown() {
        // Удаляем временный файл после каждого теста
        tempFileImport.delete();
    }

    @Test
    void testSaveCsvEmptyAndDeletingCSV() {
        String resourceDirectoryPath = "test/resources";
        File tempFile = new File(resourceDirectoryPath, "empty.csv");

        try {
            FileWriter fileWriter = new FileWriter(tempFile.toString(), StandardCharsets.UTF_8);
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertTrue(tempFile.exists(), "Ошибка при сохранении пустого файла");
        tempFile.delete();
        assertFalse(tempFile.exists(), "Ошибка при удалении пустого файла");
    }

    @Test
    void testSaveCsv() {
        String resourceDirectoryPath = "test/resources";
        File tempFile = new File(resourceDirectoryPath, "export.csv");
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);

        Task task1 = fileBackedTaskManager.createTask(new Task("Task1", "Description task1", TaskStatus.NEW, ZonedDateTime.now().plusHours(1), 10));
        Epic epic1 = fileBackedTaskManager.createEpic(new Epic("Epic2", "Description epic2"));
        SubTask subTask1 = fileBackedTaskManager.createSubTask(new SubTask("Sub Task2", "Description sub task3", TaskStatus.DONE, epic1.getId(), ZonedDateTime.now().plusHours(2), 20));

        fileBackedTaskManager.getEpicId(epic1.getId());
        fileBackedTaskManager.getSubTasksId(subTask1.getId());

        assertTrue(tempFile.exists(), "Ошибка при сохранении файла");
    }

    @Test
    void testLoadCsv() {
        //подготовим файл
        testSaveCsv();

        String resourceDirectoryPath = "test/resources";
        File tempFileExport = new File(resourceDirectoryPath, "export.csv");
        File tempFileImport = new File(resourceDirectoryPath, "import.csv");

        try {
            Files.copy(tempFileExport.toPath(), tempFileImport.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFileImport);
        fileBackedTaskManager.loadFromFile(tempFileImport);

        List<Task> loadedTasks = new ArrayList<>(fileBackedTaskManager.getListOfTasks());
        List<Epic> loadedEpics = new ArrayList<>(fileBackedTaskManager.getListOfEpics());
        List<SubTask> loadedSubTasks = new ArrayList<>(fileBackedTaskManager.getListOfSubTasks());

        List<Task> loadedHistoryTasks = fileBackedTaskManager.getHistory();

        int expectedTasksCount = 1;
        int expectedEpicsCount = 1;
        int expectedSubTasksCount = 1;
        int expectedHistoryCount = 2;

        Assertions.assertEquals(expectedTasksCount, loadedTasks.size(),
                "Количество загруженных задач не соответствует ожидаемому");
        Assertions.assertEquals(expectedEpicsCount, loadedEpics.size(),
                "Количество загруженных эпиков не соответствует ожидаемому");
        Assertions.assertEquals(expectedSubTasksCount, loadedSubTasks.size(),
                "Количество загруженных подзадач не соответствует ожидаемому");
        Assertions.assertEquals(expectedHistoryCount, loadedHistoryTasks.size(),
                "Количество загруженных задач в историю не соответствует ожидаемому");
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        String resourceDirectoryPath = "test/resources";
        File tempFileExport = new File(resourceDirectoryPath, "export.csv");
        File tempFileImport = new File(resourceDirectoryPath, "import.csv");

        try {
            Files.copy(tempFileExport.toPath(), tempFileImport.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(tempFileImport);
    }

    @Test
    void shouldThrowWhenFileNotFound() {
        File file = new File("nonefile.txt");
        assertThrows(ManagerLoadException.class, () -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(file);
            manager.loadFromFile(file);
        }, "Должно возникнуть исключение ManagerLoadException, так как файл не существует");
    }

    @Test
    void shouldNotThrowWhenFileExists() {
        String resourceDirectoryPath = "test/resources";
        File tempFileExport = new File(resourceDirectoryPath, "export.csv");
        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(tempFileExport);
            manager.loadFromFile(tempFileExport);
        }, "Не должно возникать исключений, так как файл существует");
    }

}
