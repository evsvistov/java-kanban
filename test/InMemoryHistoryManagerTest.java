import manager.InMemoryHistoryManager;
import manager.HistoryManager;
import task.Task;
import enums.TaskStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldBeEmptyInitially() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }

    @Test
    void shouldHandleDuplicates() {
        Task task = new Task("Task 1", "Description", TaskStatus.NEW);
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task); // Повторное добавление той же задачи
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дубликаты не должны добавляться в историю");
    }

    @Test
    void shouldRemoveFromStart() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(task1), "Задача должна быть удалена из начала истории");
        assertEquals(task2, history.get(0), "Вторая задача должна остаться в истории");
    }

    @Test
    void shouldRemoveFromMiddle() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", TaskStatus.NEW);
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(task2), "Задача должна быть удалена из середины истории");
        assertEquals(List.of(task1, task3), history, "Первая и третья задачи должны остаться в истории");
    }

    @Test
    void shouldRemoveFromEnd() {
        Task task1 = new Task("Task 1", "Description", TaskStatus.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description", TaskStatus.NEW);
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();
        assertFalse(history.contains(task2), "Задача должна быть удалена из конца истории");
        assertEquals(task1, history.get(0), "Первая задача должна остаться в истории");
    }
}

