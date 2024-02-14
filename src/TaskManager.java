import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private static int taskIdCounter = 1;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private int generateTaskId() {
        return taskIdCounter++;
    }

    //создание задач
    public void createTask(Task task) {
        task.setId(generateTaskId());
        if (task instanceof Epic) {
            epics.put(task.getId(), (Epic) task);
        } else if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(subTask.getEpicId());
        } else {
            tasks.put(task.getId(), task);
        }
    }

    //Получение списка всех задач
    public HashMap<Integer, Task> getListOfAllTasks() {
        HashMap<Integer, Task> listOfAllTasks = new HashMap<>();
        listOfAllTasks.putAll(tasks);
        listOfAllTasks.putAll(epics);
        listOfAllTasks.putAll(subTasks);
        return listOfAllTasks;
    }

    //Удаление всех задач
    public void deletingAllTasks() {
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    //Получение по идентификатору
    public Task getTaskId(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            task = epics.get(id);
        }
        if (task == null) {
            task = subTasks.get(id);
        }
        return task;
    }

    //Обновление
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId()) && !epics.containsKey(task.getId()) && !subTasks.containsKey(task.getId())) {
            return;
        }
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else if (epics.containsKey(task.getId())) {
            epics.put(task.getId(), (Epic) task);
        } else if (subTasks.containsKey(task.getId())) {
            SubTask subTask = (SubTask) task;
            subTasks.put(task.getId(), subTask);
            updateEpicStatus(subTask.getEpicId());
        }
    }

    //Удаление по идентификатору.
    public void deleteTask(int id) {
        Task task = getTaskId(id);
        if (task == null) {
            return;
        }

        if (task instanceof Epic) {
            ArrayList<Integer> subTaskIdsToRemove = new ArrayList<>();
            for (SubTask subTask : subTasks.values()) {
                if (subTask.getEpicId() == id) {
                    subTaskIdsToRemove.add(subTask.getId());
                }
            }
            for (int subTaskId : subTaskIdsToRemove) {
                subTasks.remove(subTaskId);
            }
            epics.remove(id);
        } else if (task instanceof SubTask) {
            subTasks.remove(id);
        } else {
            tasks.remove(id);
        }
    }

    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        boolean allSubTasksNew = true;
        boolean allSubTasksDone = true;

        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicId() == epicId) {
                if (subTask.getStatus() != TaskStatus.NEW) {
                    allSubTasksNew = false;
                }
                if (subTask.getStatus() != TaskStatus.DONE) {
                    allSubTasksDone = false;
                }
            }
        }
        if (allSubTasksDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allSubTasksNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}