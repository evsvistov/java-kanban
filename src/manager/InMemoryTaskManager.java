package manager;

import enums.TaskStatus;
import exceptions.TaskTimeConflictException;
import task.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int taskIdCounter = 1;
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

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
    public ArrayList<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getListOfSubTasks() {
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
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtasks().forEach(historyManager::remove);
        });
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deletingAllsubTasks() {
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.clear();

        epics.values().forEach(epic -> {
            epic.deleteAllSubtask();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        });
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
    public Epic getEpicId(int id) {
        Epic task = epics.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public SubTask getSubTasksId(int id) {
        SubTask task = subTasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        if (prioritizedTasks.stream().anyMatch(existingTask -> taskTimeIntersection(task, existingTask))) {
            throw new TaskTimeConflictException("Новая задача конфликтует с существующими задачами.");
        }
        task.setId(generateTaskId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
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
            if (prioritizedTasks.stream().anyMatch(existingTask -> taskTimeIntersection(subTask, existingTask))) {
                throw new TaskTimeConflictException("Новая подзадача конфликтует с существующими задачами.");
            }
            subTask.setId(generateTaskId());
            subTasks.put(subTask.getId(), subTask);
            epics.get(subTask.getEpicId()).addSubtask(subTask.getId());
            updateEpicStatus(subTask.getEpicId());
            updateEpicTime(subTask.getEpicId());
            prioritizedTasks.add(subTask);
        }
        return subTask;
    }

    //Обновление
    @Override
    public boolean updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask);

            if (prioritizedTasks.stream().anyMatch(existingTask -> taskTimeIntersection(task, existingTask))) {
                prioritizedTasks.add(oldTask);
                throw new TaskTimeConflictException("Обновленная задача конфликтует с существующими задачами.");
            }

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
            SubTask oldSubTask = subTasks.get(subTask.getId());
            prioritizedTasks.remove(oldSubTask);

            if (prioritizedTasks.stream().anyMatch(existingTask -> taskTimeIntersection(subTask, existingTask))) {
                prioritizedTasks.add(oldSubTask);
                throw new TaskTimeConflictException("Обновленная подзадача конфликтует с существующими задачами.");
            }

            SubTask currentSubTask = subTasks.get(subTask.getId());
            if (currentSubTask.getEpicId() == (subTask.getEpicId())) {
                subTasks.put(subTask.getId(), subTask);
                prioritizedTasks.add(subTask);
                updateEpicStatus(subTask.getEpicId());
                updateEpicTime(subTask.getEpicId());
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
        if (epic != null && !epic.getSubtasks().isEmpty()) {
            epic.getSubtasks().forEach(taskId -> {
                subTasks.remove(taskId);
                historyManager.remove(taskId);
            });
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
        updateEpicTime(subTask.getEpicId());
    }

    //получение списка всех подзадач определённого эпика
    @Override
    public List<SubTask> getListSubTaskOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtasks().stream() // Создаем стрим из списка идентификаторов подзадач
                .map(subTasks::get) // Преобразуем каждый идентификатор в объект SubTask, используя карту subTasks
                .toList(); // Собираем результаты в список
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public boolean taskTimeIntersection(Task task1, Task task2) {
        ZonedDateTime start1 = task1.getStartTime();
        ZonedDateTime start2 = task2.getStartTime();

        if (start1 == null || start2 == null) {
            return false;
        }

        ZonedDateTime end1 = task1.getEndTime();
        ZonedDateTime end2 = task2.getEndTime();

        return (start1.isBefore(end2) || start1.equals(start2) || start1.equals(end2))
                && (start2.isBefore(end1) || start2.equals(start1) || start2.equals(end1))
                && (end1.isAfter(start2) || end1.equals(end2))
                && (end2.isAfter(start1) || end2.equals(end1));
    }

    protected void setStartGenerateTaskId(int taskIdCounter) {
        this.taskIdCounter = taskIdCounter;
    }

    private int generateTaskId() {
        return taskIdCounter++;
    }

    //пересчёт времени выполнения эпика
    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        List<SubTask> subTaskList = epic.getSubtasks().stream()
                .map(subTasks::get)
                .toList();

        if (!subTaskList.isEmpty()) {
            //находим минимальное время начала среди всех подзадач
            Optional<ZonedDateTime> minStartTime = subTaskList.stream()
                    .map(SubTask::getStartTime)
                    .filter(Objects::nonNull)
                    .min(ZonedDateTime::compareTo);

            //находим максимальное время начала среди всех подзадач
            Optional<ZonedDateTime> maxEndTime = subTaskList.stream()
                    .map(SubTask::getEndTime)
                    .filter(Objects::nonNull)
                    .max(ZonedDateTime::compareTo);

            //вычисляем общую продолжительность как разницу между кнчаниями макс. и мин. всех подзадач
            Duration totalDuration = Duration.between(minStartTime.orElse(ZonedDateTime.now()), maxEndTime.orElse(ZonedDateTime.now()));

            epic.setStartTime(minStartTime.orElse(null));
            epic.setDuration(totalDuration);
            epic.setEndTime(maxEndTime.orElse(null));
        } else {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
        }
    }

    //пересчёт статуса эпика
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<Integer> subTaskList = epic.getSubtasks();
        if (!epic.getSubtasks().isEmpty()) {
            boolean allSubTasksNew = subTaskList.stream()
                    .map(subTasks::get)
                    .filter(subTask -> subTask.getEpicId() == epicId)
                    .allMatch(subTask -> subTask.getStatus() == TaskStatus.NEW);

            boolean allSubTasksDone = subTaskList.stream()
                    .map(subTasks::get)
                    .filter(subTask -> subTask.getEpicId() == epicId)
                    .allMatch(subTask -> subTask.getStatus() == TaskStatus.DONE);

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
