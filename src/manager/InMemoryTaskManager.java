package manager;

import task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private int taskIdCounter = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //Получение списка всех задач
    @Override
    public ArrayList<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Task> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Task> getListOfSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    //Удаление всех задач
    @Override
    public void deletingAllTasks() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void deletingAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (int idSubTask : epic.getSubtasks()) {
                historyManager.remove(idSubTask);
            }
        }
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deletingAllsubTasks() {
        for (int idSubTask : subTasks.keySet()) {
            historyManager.remove(idSubTask);
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtask();
            updateEpicStatus(epic.getId());
        }
    }

    //Получение по идентификатору
    @Override
    public Task getTaskId(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task getEpicId(int id) {
        Epic task = epics.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task getSubTasksId(int id) {
        SubTask task = subTasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        task.setId(generateTaskId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateTaskId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        if (epics.get(subTask.getEpicId()) != null) {
            subTask.setId(generateTaskId());
            subTasks.put(subTask.getId(), subTask);
            epics.get(subTask.getEpicId()).addSubtask(subTask.getId());
            updateEpicStatus(subTask.getEpicId());
        }
        return subTask;
    }

    //Обновление
    @Override
    public boolean updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic currentEpic = epics.get(epic.getId());
            currentEpic.setName(epic.getName());
            currentEpic.setDescription(epic.getDescription());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTasks.containsKey(subTask.getId())) {
            SubTask currentSubTask = subTasks.get(subTask.getId());
            if (currentSubTask.getEpicId() == (subTask.getEpicId())) {
                subTasks.put(subTask.getId(), subTask);
                updateEpicStatus(subTask.getEpicId());
            }
            return true;
        }
        return false;
    }

    //Удаление по идентификатору
    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (!epic.getSubtasks().isEmpty()) {
            for (int task : epic.getSubtasks()) {
                subTasks.remove(task);
                historyManager.remove(task);
            }
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        Epic epic = epics.get(subTask.getEpicId());
        epic.deleteSubtaskId(id);
        subTasks.remove(id);
        historyManager.remove(id);
        updateEpicStatus(subTask.getEpicId());
    }

    //Получение списка всех подзадач определённого эпика
    @Override
    public ArrayList<SubTask> getListSubTaskOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>();
        if (!epic.getSubtasks().isEmpty()) {
            for (int subTuskId : epic.getSubtasks()) {
                subTasksOfEpic.add(subTasks.get(subTuskId));
            }
        }
        return subTasksOfEpic;
    }

    private int generateTaskId() {
        return taskIdCounter++;
    }

    //пересчёт статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (!epic.getSubtasks().isEmpty()) {
            boolean allSubTasksNew = true;
            boolean allSubTasksDone = true;
            for (int task : epic.getSubtasks()) {
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
            epic.setStatus(TaskStatus.NEW);
        }
    }

}
