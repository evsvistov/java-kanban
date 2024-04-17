import enums.TaskStatus;
import manager.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T taskManager;
    protected ZonedDateTime fixedTime;

    @BeforeEach
    public void setUp() {
        taskManager = createTaskManager();
        fixedTime = ZonedDateTime.now();
    }

    protected abstract T createTaskManager();

    // Тесты для методов TaskManager
    @Test
    void testAllSubtasksNew() {
        Epic epic = taskManager.createEpic(new Epic("Epic with NEW subtasks", "Description"));
        taskManager.createSubTask(new SubTask("SubTask 1_NEW", "Description", TaskStatus.NEW, epic.getId(),
                fixedTime.plusHours(1), 15));
        taskManager.createSubTask(new SubTask("SubTask 2_NEW", "Description", TaskStatus.NEW, epic.getId(),
                fixedTime.plusHours(2), 15));
        assertEquals(TaskStatus.NEW, epic.getStatus(), "Статус эпика должен быть NEW для всех подзач в статусе NEW");
    }

    @Test
    void testAllSubtasksDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic with DONE subtasks", "Description"));
        taskManager.createSubTask(new SubTask("SubTask 1_DONE", "Description", TaskStatus.DONE, epic.getId(),
                fixedTime.plusHours(1), 15));
        taskManager.createSubTask(new SubTask("SubTask 2_DONE", "Description", TaskStatus.DONE, epic.getId(),
                fixedTime.plusHours(2), 15));
        assertEquals(TaskStatus.DONE, epic.getStatus(), "Статус эпика должен быть DONE для всех подзач в статусе DONE");
    }

    @Test
    void testSubtasksNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic("Epic with NEW and DONE subtasks", "Description"));
        taskManager.createSubTask(new SubTask("SubTask 1_NEW_DONW", "Description", TaskStatus.NEW, epic.getId(),
                fixedTime.plusHours(1), 15));
        taskManager.createSubTask(new SubTask("SubTask 2_NEW_DONW", "Description", TaskStatus.DONE, epic.getId(),
                fixedTime.plusHours(2), 15));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS для подзач в разных статусах");
    }

    @Test
    void testSubtasksInProgress() {
        Epic epic = taskManager.createEpic(new Epic("Epic with IN_PROGRESS subtasks", "Description"));
        taskManager.createSubTask(new SubTask("SubTask 1_IN_PROGRESS", "Description", TaskStatus.IN_PROGRESS, epic.getId(),
                fixedTime.plusHours(1), 15));
        taskManager.createSubTask(new SubTask("SubTask 2_IN_PROGRESS", "Description", TaskStatus.IN_PROGRESS, epic.getId(),
                fixedTime.plusHours(2), 15));
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS для всех подзач в статусе IN_PROGRESS");
    }

    @Test
    void subtaskShouldHaveValidEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic for subtasks", "Description"));
        SubTask subTask = new SubTask("SubTask", "Description", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 1);
        SubTask createdSubTask = taskManager.createSubTask(subTask);
        assertNotNull(createdSubTask);
        assertNotNull(taskManager.getEpicId(createdSubTask.getEpicId()), "Подзадача должна иметь существующий эпик");
        assertEquals(epic.getId(), createdSubTask.getEpicId(), "ID эпика подзадачи должен соответствовать созданному эпику");
    }

    @Test
    void epicStatusShouldBeCalculatedCorrectly() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));
        SubTask subTask1 = new SubTask("SubTask 1", "Description", TaskStatus.NEW, epic.getId(), ZonedDateTime.now(), 1);
        SubTask subTask2 = new SubTask("SubTask 2", "Description", TaskStatus.DONE, epic.getId(), ZonedDateTime.now().plusHours(2), 1);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS, если подзадачи имеют разные статусы");
    }

    @Test
    void tasksShouldNotOverlap() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", TaskStatus.NEW, ZonedDateTime.now(), 2));
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW, ZonedDateTime.now().plusHours(3), 2);
        assertFalse(taskManager.taskTimeIntersection(task1, task2), "Задачи не должны пересекаться по времени");
    }

    @Test
    void tasksShouldOverlap() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Description", TaskStatus.NEW, ZonedDateTime.now(), 2));
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW, ZonedDateTime.now(), 2);
        assertTrue(taskManager.taskTimeIntersection(task1, task2), "Задачи не пересекаются по времени");
    }


}
