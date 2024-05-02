package manager;

import task.*;

import java.util.List;
import java.util.Set;

public interface TaskManager {

    List<Task> getHistory();

    List<Task> getListOfTasks();

    List<Epic> getListOfEpics();

    List<SubTask> getListOfSubTasks();

    void deletingAllTasks();

    void deletingAllEpics();

    void deletingAllsubTasks();

    Task getTaskId(int id);

    Epic getEpicId(int id);

    SubTask getSubTasksId(int id);

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subTask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubTask(SubTask subTask);

    boolean deleteTask(int id);

    boolean deleteEpic(int id);

    boolean deleteSubTask(int id);

    List<SubTask> getListSubTaskOfEpic(int epicId);

    boolean taskTimeIntersection(Task task1, Task task2);

    Set<Task> getPrioritizedTasks();
}
