import enums.TaskStatus;
import manager.*;
import task.*;

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
        Task task1 = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        Task task2 = taskManager.getTaskId(task1.getId());
        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    void tasksEqualityBasedOnId() {
        // Создаем и добавляем задачи разных типов в менеджер задач
        Task task1 = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("SubTask 1", "SubTask 1 Description", TaskStatus.NEW, epic1.getId()));

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
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description Epic"));
        SubTask subTask = new SubTask("SubTask 1", " SubTask 1 Description SubTask", TaskStatus.NEW, epic.getId());

        epic.setId(subTask.getId());
        taskManager.updateEpic(epic);

        assertFalse(taskManager.getListOfSubTasks().contains(epic), "Epic нельзя добавить в самого себя в виде подзадачи");
    }

    //проверьте, что объект Subtask нельзя сделать своим же эпиком
    @Test
    void subtaskCannotBeMadeIntoItsOwnEpic() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));

        SubTask subTask = new SubTask("SubTask 1", "SubTask 1 Description", TaskStatus.NEW, epic1.getId());
        subTask.setId(epic1.getId());
        taskManager.updateSubTask(subTask);

        assertFalse(taskManager.getListOfEpics().contains(subTask), "Subtask нельзя сделать своим же эпиком");
    }

    //убедитесь, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров
    @Test
    void shouldReturnInitializedTaskManager() {
        assertNotNull(taskManager, "Утилитарный класс InMemoryTaskManager должен быть проинициализирован");
        assertNotNull(historyManager, "Утилитарный класс HistoryManager должен быть проинициализирован");
    }

    //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id
    @Test
    void createTask() {
        Task task = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        final Task savedTask = taskManager.getTaskId(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getListOfTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void createEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        final Task savedEpic = taskManager.getEpicId(epic.getId());

        assertNotNull(savedEpic, "Эпики не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getListOfEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void createSubTask() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        SubTask subTask = taskManager.createSubTask(new SubTask("SubTask 1", "SubTask 1 Description",
                TaskStatus.NEW, epic.getId()));

        final Task savedSubTask = taskManager.getSubTasksId(subTask.getId());

        assertNotNull(savedSubTask, "Сабтаска не найдена.");
        assertEquals(subTask, savedSubTask, "Сабтаски не совпадают.");

        final List<SubTask> subTasks = taskManager.getListOfSubTasks();

        assertNotNull(subTasks, "Сабтаски не возвращаются.");
        assertEquals(1, subTasks.size(), "Неверное количество сабтасков.");
        assertEquals(subTask, subTasks.get(0), "Сабтаски не совпадают.");
    }

    //проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    void taskIdsDoNotConflict() {
        Task firstTask = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        Task secondTask = taskManager.createTask(new Task("Task 2", "Task 2 Description", TaskStatus.NEW));

        assertNotEquals(firstTask.getId(), secondTask.getId(), "ID задач конфликтуют");

        Task fetchedFirstTask = taskManager.getTaskId(firstTask.getId());
        assertNotNull(fetchedFirstTask, "Первая задача не найдена");
        assertEquals(firstTask, fetchedFirstTask, "Первая задача не соответствует ожидаемой");

        Task fetchedSecondTask = taskManager.getTaskId(secondTask.getId());
        assertNotNull(fetchedSecondTask, "Вторая задача не найдена.");
        assertEquals(secondTask, fetchedSecondTask, "Вторая задача не соответствует ожидаемой");
    }

    //создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    void taskImmutableAllFields() {
        Task originalTask = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));

        assertEquals(originalTask.getName(), "Task 1", "Название задачи изменилось");
        assertEquals(originalTask.getDescription(), "Task 1 Description", "Описание задачи изменилось");
        assertEquals(originalTask.getStatus(), TaskStatus.NEW, "Статус задачи изменился");
    }

    //проверка добавления в историю
    @Test
    void addHistory() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Task 2 Description", TaskStatus.NEW));
        taskManager.getTaskId(task1.getId());
        taskManager.getTaskId(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertTrue(history.contains(task1) && history.contains(task2), "История должна содержать обе задачи.");
    }

    //проверка удаления из истории
    @Test
    void removeHistoryTask() {
        Task task = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        taskManager.getTaskId(task.getId());

        taskManager.deleteTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления задачи.");
    }

    @Test
    void removeHistorySubTask() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        SubTask subTask = taskManager.createSubTask(new SubTask("SubTask 1", "SubTask 1 Description",
                TaskStatus.NEW, epic.getId()));

        taskManager.getEpicId(epic.getId());
        taskManager.getSubTasksId(subTask.getId());

        taskManager.deleteSubTask(subTask.getId());
        Epic retrievedEpic = (Epic) taskManager.getEpicId(epic.getId());
        assertFalse(retrievedEpic.getSubtasks().contains(subTask.getId()), "ID удалённой подзадачи не должно присутствовать в списке ID подзадач эпика.");
    }

    @Test
    void removeHistoryEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        SubTask subTask = taskManager.createSubTask(new SubTask("SubTask 1", "SubTask 1 Description",
                TaskStatus.NEW, epic.getId()));

        taskManager.getEpicId(epic.getId());
        taskManager.getSubTasksId(subTask.getId());

        taskManager.deleteEpic(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления эпика.");
    }

    //изменение задач через сеттеры
    @Test
    void changingTasksSetters() {
        Task task = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        task.setName("Task 1 Updated Name");

        taskManager.updateTask(task);

        Task retrievedTask = taskManager.getTaskId(task.getId());
        assertEquals("Task 1 Updated Name", retrievedTask.getName(), "Имя задачи должно обновиться.");
    }
}