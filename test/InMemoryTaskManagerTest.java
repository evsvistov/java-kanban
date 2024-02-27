import org.junit.jupiter.api.AfterEach;
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

    //проверьте, что экземпляры класса Task равны друг другу, если равен их id
    @Test
    void tasksShouldBeEqualToEachOther() {
        Task task1 = taskManager.createTask(new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW));
        Task task2 = taskManager.getTaskId(task1.getId());
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    void tasksEqualityBasedOnId() {
        // Создаем и добавляем задачи разных типов в менеджер задач
        Task task1 = taskManager.createTask(new Task("Task", "Description Task", TaskStatus.NEW));
        Epic epic1 = taskManager.createEpic(new Epic("Epic", "Description Epic"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("SubTask", "Description SubTask", TaskStatus.NEW, epic1.getId()));

        Task task2 = new Task(task1.getName(), task1.getDescription(), task1.getStatus());
        task2.setId(task1.getId());

        Epic epic2 = new Epic(epic1.getName(), epic1.getDescription());
        epic2.setId(epic1.getId());

        SubTask subTask2 = new SubTask(subTask1.getName(), subTask1.getDescription(), subTask1.getStatus(), subTask1.getEpicId());
        subTask2.setId(subTask1.getId());
        epic2.addSubtask(subTask2.getId());

        assertEquals(task1, task2, "Задачи с одинаковым идентификатором должны быть равными");
        assertEquals(epic1, epic2, "Эпики с одинаковым идентификатором должны быть равными");
        assertEquals(subTask1, subTask2, "Сабтаски с одинаковым идентификатором должны быть равными");
    }

    //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void epicCannotBeAddedToItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Description Epic");
        int epicId = taskManager.createEpic(epic).getId();

        SubTask subTask = new SubTask("SubTask for Epic", "Description SubTask", TaskStatus.NEW, epicId);
        epic.setId(subTask.getId());
        taskManager.updateEpic(epic);

        assertFalse(taskManager.getListOfSubTasks().contains(epic), "Epic нельзя добавить в самого себя в виде подзадачи");
    }

    //проверьте, что объект Subtask нельзя сделать своим же эпиком
    @Test
    void subtaskCannotBeMadeIntoItsOwnEpic() {
        Epic epic1 = new Epic("Epic", "Description Epic");
        int epicId = taskManager.createEpic(epic1).getId();

        SubTask subTask = new SubTask("SubTask for Epic", "Description SubTask", TaskStatus.NEW, epicId);
        subTask.setId(epicId);
        taskManager.updateSubTask(subTask);

        assertFalse(taskManager.getListOfEpics().contains(subTask), "Subtask нельзя сделать своим же эпиком");
    }

    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    public void shouldReturnInitializedTaskManager() {
        assertNotNull(taskManager, "Утилитарный класс InMemoryTaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "Утилитарный класс HistoryManager должен быть проинициализирован");
    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void createTask() {
        Task task = new Task("Task", "Description Task", TaskStatus.NEW);
        final int taskId = taskManager.createTask(task).getId();
        final Task savedTask = taskManager.getTaskId(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getListOfTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Epic", "Description Epic");
        final int epicId = taskManager.createEpic(epic).getId();
        final Task savedEpic = taskManager.getEpicId(epicId);

        assertNotNull(savedEpic, "Эпики не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Task> epics = taskManager.getListOfEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void createSubTask() {
        Epic epic = new Epic("Epic", "Description Epic");
        final int epicId = taskManager.createEpic(epic).getId();
        SubTask subTask = new SubTask("Test addNewSubTask", "Test addNewSubTask description",
                TaskStatus.NEW, epicId);

        final int subTaskId = taskManager.createSubTask(subTask).getId();
        final Task savedSubTask = taskManager.getSubTasksId(subTaskId);

        assertNotNull(savedSubTask, "Сабтаска не найдена.");
        assertEquals(subTask, savedSubTask, "Сабтаски не совпадают.");

        final List<Task> subTasks = taskManager.getListOfSubTasks();

        assertNotNull(subTasks, "Сабтаски не возвращаются.");
        assertEquals(1, subTasks.size(), "Неверное количество сабтасков.");
        assertEquals(subTask, subTasks.get(0), "Сабтаски не совпадают.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    void taskIdsDoNotConflict() {
        Task firstTask = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        int firstTaskId = firstTask.getId();

        Task secondTask = taskManager.createTask(new Task("Task 2", "Task 2 Description", TaskStatus.NEW));
        int secondTaskId = secondTask.getId();

        assertNotEquals(firstTaskId, secondTaskId, "ID задач конфликтуют");

        Task fetchedFirstTask = taskManager.getTaskId(firstTaskId);
        assertNotNull(fetchedFirstTask, "Первая задача не найдена");
        assertEquals(firstTask, fetchedFirstTask, "Первая задача не соответствует ожидаемой");

        Task fetchedSecondTask = taskManager.getTaskId(secondTaskId);
        assertNotNull(fetchedSecondTask, "Вторая задача не найдена.");
        assertEquals(secondTask, fetchedSecondTask, "Вторая задача не соответствует ожидаемой");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskImmutableAllFields() {
        Task originalTask = new Task("Task Title", "Task Description", TaskStatus.NEW);
        Task addedTask = taskManager.createTask(originalTask);

        Task fetchedTask = taskManager.getTaskId(addedTask.getId());

        assertEquals(originalTask.getName(), "Task Title", "Название задачи изменилось");
        assertEquals(originalTask.getDescription(), "Task Description", "Описание задачи изменилось");
        assertEquals(originalTask.getStatus(), TaskStatus.NEW, "Статус задачи изменился");
    }

    //проверка добавления в историю
    @Test
    void addHistory() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    //убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    void taskHistoryPreservesChanges() {
        Task task = new Task("Task 1", "Task 1 Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTaskId(task.getId());
        assertNotNull(fetchedTask);

        Task addTask = new Task(fetchedTask.getName(), fetchedTask.getDescription(), fetchedTask.getStatus());

        addTask.setId(task.getId());
        addTask.setName("Updated Task 1");
        addTask.setDescription("Updated Task 1 Description");
        taskManager.updateTask(addTask);

        final List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());

        Task historyTask = history.get(0);
        assertEquals("Task 1", historyTask.getName(), "Имя в истории не соответствует первоначальному");
        assertEquals("Task 1 Description", historyTask.getDescription(), "Описание в истории не соответствует первоначальному");
    }

}