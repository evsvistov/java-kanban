import Manager.*;
import Task.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private HistoryManager historyManager;
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
        taskManager = new InMemoryTaskManager();
    }

    //проверьте, что экземпляры класса Task.Task равны друг другу, если равен их id
    @Test
    void tasksShouldBeEqualToEachOther() {
        Task task1 = taskManager.createTask(new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW));
        Task task2 = taskManager.getTaskId(task1.getId());
        Assertions.assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    //проверьте, что наследники класса Task.Task равны друг другу, если равен их id;
    @Test
    void tasksEqualityBasedOnId() {
        // Создаем и добавляем задачи разных типов в менеджер задач
        Task task1 = taskManager.createTask(new Task("Task", "Description Task.Task", TaskStatus.NEW));
        Epic epic1 = taskManager.createEpic(new Epic("Task.Epic", "Description Task.Epic"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("Task.SubTask", "Description Task.SubTask", TaskStatus.NEW, epic1.getId()));

        Task task2 = new Task(task1.getName(), task1.getDescription(), task1.getStatus());
        task2.setId(task1.getId());

        Epic epic2 = new Epic(epic1.getName(), epic1.getDescription());
        epic2.setId(epic1.getId());

        SubTask subTask2 = new SubTask(subTask1.getName(), subTask1.getDescription(), subTask1.getStatus(), subTask1.getEpicId());
        subTask2.setId(subTask1.getId());
        epic2.addSubtask(subTask2.getId());

        Assertions.assertEquals(task1, task2, "Задачи с одинаковым идентификатором должны быть равными");
        assertEquals(epic1, epic2, "Эпики с одинаковым идентификатором должны быть равными");
        assertEquals(subTask1, subTask2, "Сабтаски с одинаковым идентификатором должны быть равными");
    }

    //проверьте, что объект Task.Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void epicCannotBeAddedToItselfAsSubtask() {
        Epic epic = new Epic("Task.Epic", "Description Task.Epic");
        int epicId = taskManager.createEpic(epic).getId();

        SubTask subTask = new SubTask("Task.SubTask for Task.Epic", "Description Task.SubTask", TaskStatus.NEW, epicId);
        epic.setId(subTask.getId());
        taskManager.updateEpic(epic);

        Assertions.assertFalse(taskManager.getListOfSubTasks().contains(epic), "Task.Epic нельзя добавить в самого себя в виде подзадачи");
    }

    //проверьте, что объект Subtask нельзя сделать своим же эпиком
    @Test
    void subtaskCannotBeMadeIntoItsOwnEpic() {
        Epic epic1 = new Epic("Task.Epic", "Description Task.Epic");
        int epicId = taskManager.createEpic(epic1).getId();

        SubTask subTask = new SubTask("Task.SubTask for Task.Epic", "Description Task.SubTask", TaskStatus.NEW, epicId);
        subTask.setId(epicId);
        taskManager.updateSubTask(subTask);

        Assertions.assertFalse(taskManager.getListOfEpics().contains(subTask), "Subtask нельзя сделать своим же эпиком");
    }

    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnInitializedTaskManager() {
        Assertions.assertNotNull(taskManager, "Утилитарный класс Manager.InMemoryTaskManager должен быть проинициализирован");
        Assertions.assertNotNull(historyManager, "Утилитарный класс Manager.HistoryManager должен быть проинициализирован");
    }

    //проверьте, что Manager.InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void createTask() {
        Task task = new Task("Task", "Description Task.Task", TaskStatus.NEW);
        final int taskId = taskManager.createTask(task).getId();
        final Task savedTask = taskManager.getTaskId(taskId);

        Assertions.assertNotNull(savedTask, "Задача не найдена.");
        Assertions.assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getListOfTasks();

        Assertions.assertNotNull(tasks, "Задачи не возвращаются.");
        Assertions.assertEquals(1, tasks.size(), "Неверное количество задач.");
        Assertions.assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Task.Epic", "Description Task.Epic");
        final int epicId = taskManager.createEpic(epic).getId();
        final Task savedEpic = taskManager.getEpicId(epicId);

        Assertions.assertNotNull(savedEpic, "Эпики не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Task> epics = taskManager.getListOfEpics();

        Assertions.assertNotNull(epics, "Эпики не возвращаются.");
        Assertions.assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void createSubTask() {
        Epic epic = new Epic("Task.Epic", "Description Task.Epic");
        final int epicId = taskManager.createEpic(epic).getId();
        SubTask subTask = new SubTask("Test addNewSubTask", "Test addNewSubTask description",
                TaskStatus.NEW, epicId);

        final int subTaskId = taskManager.createSubTask(subTask).getId();
        final Task savedSubTask = taskManager.getSubTasksId(subTaskId);

        Assertions.assertNotNull(savedSubTask, "Сабтаска не найдена.");
        assertEquals(subTask, savedSubTask, "Сабтаски не совпадают.");

        final List<Task> subTasks = taskManager.getListOfSubTasks();

        Assertions.assertNotNull(subTasks, "Сабтаски не возвращаются.");
        Assertions.assertEquals(1, subTasks.size(), "Неверное количество сабтасков.");
        assertEquals(subTask, subTasks.get(0), "Сабтаски не совпадают.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    void taskIdsDoNotConflict() {
        Task firstTask = taskManager.createTask(new Task("Task.Task 1", "Task.Task 1 Description", TaskStatus.NEW));
        int firstTaskId = firstTask.getId();

        Task secondTask = taskManager.createTask(new Task("Task.Task 2", "Task.Task 2 Description", TaskStatus.NEW));
        int secondTaskId = secondTask.getId();

        Assertions.assertNotEquals(firstTaskId, secondTaskId, "ID задач конфликтуют");

        Task fetchedFirstTask = taskManager.getTaskId(firstTaskId);
        Assertions.assertNotNull(fetchedFirstTask, "Первая задача не найдена");
        Assertions.assertEquals(firstTask, fetchedFirstTask, "Первая задача не соответствует ожидаемой");

        Task fetchedSecondTask = taskManager.getTaskId(secondTaskId);
        Assertions.assertNotNull(fetchedSecondTask, "Вторая задача не найдена.");
        Assertions.assertEquals(secondTask, fetchedSecondTask, "Вторая задача не соответствует ожидаемой");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskImmutableAllFields() {
        Task originalTask = new Task("Task.Task Title", "Task.Task Description", TaskStatus.NEW);
        Task addedTask = taskManager.createTask(originalTask);

        Task fetchedTask = taskManager.getTaskId(addedTask.getId());

        Assertions.assertEquals(originalTask.getName(), "Task.Task Title", "Название задачи изменилось");
        Assertions.assertEquals(originalTask.getDescription(), "Task.Task Description", "Описание задачи изменилось");
        assertEquals(originalTask.getStatus(), TaskStatus.NEW, "Статус задачи изменился");
    }

    //проверка добавления в историю
    @Test
    void addHistory() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        Assertions.assertNotNull(history, "История не пустая.");
        Assertions.assertEquals(1, history.size(), "История не пустая.");
    }

    //убедитесь, что задачи, добавляемые в Manager.HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void taskHistoryPreservesChanges() {
        Task task = new Task("Task.Task 1", "Task.Task 1 Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTaskId(task.getId());
        Assertions.assertNotNull(fetchedTask);

        Task addTask = new Task(fetchedTask.getName(), fetchedTask.getDescription(), fetchedTask.getStatus());

        addTask.setId(task.getId());
        addTask.setName("Updated Task.Task 1");
        addTask.setDescription("Updated Task.Task 1 Description");
        taskManager.updateTask(addTask);

        final List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(1, history.size());

        Task historyTask = history.get(0);
        Assertions.assertEquals("Task.Task 1", historyTask.getName(), "Имя в истории не соответствует первоначальному");
        Assertions.assertEquals("Task.Task 1 Description", historyTask.getDescription(), "Описание в истории не соответствует первоначальному");
    }

}