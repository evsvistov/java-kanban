import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private static int taskIdCounter = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();

    private static int generateTaskId() {
        return taskIdCounter++;
    }

    //Получение списка всех задач
    public ArrayList<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Task> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Task> getListOfSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    //Удаление всех задач
    public void deletingAllTasks() {
        tasks.clear();
    }

    public void deletingAllEpics() {
        epics.clear();
    }

    public void deletingAllsubTasks() {
        subTasks.clear();
    }

    //Получение по идентификатору
    public Task getTaskId(int id) {
        return tasks.get(id);
    }

    public Task getEpicId(int id) {
        return epics.get(id);
    }

    public Task getSubTasksId(int id) {
        return subTasks.get(id);
    }

    //создание задач
    public Task createTask(Task task) {
        task.setId(generateTaskId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createTask(Epic epic) {
        epic.setId(generateTaskId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createTask(SubTask subTask) {
        if (epics.get(subTask.getEpicId()) != null) {
            subTask.setId(generateTaskId());
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(subTask.getEpicId());
        }
        return subTask;
    }

    //Обновление
    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
        return task;
    }

    public Task updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
            updateEpicStatus(epic.getId());
        }
        return epic;
    }

    public Task updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            subTasks.put(subTask.getId(), subTask);
            updateEpicStatus(subTask.getEpicId());
        }
        return subTask;
    }

    //Удаление по идентификатору
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (!epic.getSubtasks().isEmpty()) {
            for (int task : epics.get(id).getSubtasks()) {
                subTasks.remove(task);
            }
        }
        epics.remove(id);
    }

    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        Epic epic = epics.get(subTask.getEpicId());
        epic.deleteSubtaskId(id);
        subTasks.remove(id);
        updateEpicStatus(subTask.getEpicId());
    }

    //пересчёт статуса эпика
    private void updateEpicStatus(int epicId) {
        if (!epics.get(epicId).getSubtasks().isEmpty()) {
            Epic epic = epics.get(epicId);
            boolean allSubTasksNew = true;
            boolean allSubTasksDone = true;

            for (int task : epics.get(epicId).getSubtasks()) {
                SubTask subTask = subTasks.get(task);
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

        } else {
            epics.get(epicId).setStatus(TaskStatus.NEW);
        }
    }

    //Получение списка всех подзадач определённого эпика
    public ArrayList<SubTask> getListSubTaskOfEpic(int epicId) {
        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>();
        if (!epics.get(epicId).getSubtasks().isEmpty()) {
            Epic epic = epics.get(epicId);
            for (int subTuskId : epic.getSubtasks()) {
                subTasksOfEpic.add(subTasks.get(subTuskId));
            }
        }
        return subTasksOfEpic;
    }

}