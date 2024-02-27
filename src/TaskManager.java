import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    List<Task> getHistory();
    ArrayList<Task> getListOfTasks();

    ArrayList<Task> getListOfEpics();

    ArrayList<Task> getListOfSubTasks();

    void deletingAllTasks();

    void deletingAllEpics();

    void deletingAllsubTasks();

    Task getTaskId(int id);

    Task getEpicId(int id);

    Task getSubTasksId(int id);

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subTask);

    boolean updateTask(Task task);

    boolean updateEpic(Epic epic);

    boolean updateSubTask(SubTask subTask);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubTask(int id);

    ArrayList<SubTask> getListSubTaskOfEpic(int epicId);

    int generateTaskId();

    void updateEpicStatus(int epicId);
}
