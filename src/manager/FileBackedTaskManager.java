package manager;

import enums.TaskStatus;
import enums.TaskType;
import exceptions.ManagerLoadException;
import exceptions.ManagerSaveException;
import task.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public FileBackedTaskManager loadFromFile(File file) throws ManagerLoadException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            int maxIdInFile = getMaxIdInFile(lines);
            fileBackedTaskManager.setStartGenerateTaskId(maxIdInFile + 1);
            boolean isHistory = false;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) {
                    isHistory = true;
                    continue;
                }
                if (!isHistory) {
                    fromString(lines[i]);
                } else {
                    List<Integer> historyList = historyFromString(lines[i]);
                    historyList.forEach(taskId -> {
                        if (tasks.containsKey(taskId)) {
                            historyManager.add(tasks.get(taskId));
                        } else if (epics.containsKey(taskId)) {
                            historyManager.add(epics.get(taskId));
                        } else if (subTasks.containsKey(taskId)) {
                            historyManager.add(subTasks.get(taskId));
                        }
                    });
                }
            }
            subTasks.values().forEach(subTask -> {
                Epic epic = epics.get(subTask.getEpicId());
                if (epic != null) {
                    epic.addSubtask(subTask.getId());
                }
            });
        } catch (IOException e) {
            throw new ManagerLoadException("Произошла ошибка при чтении файла", e);
        }
        return fileBackedTaskManager;
    }

    public List<Task> getHistoryManager() {
        return super.getHistory();
    }

    //Удаление всех задач
    @Override
    public void deletingAllTasks() {
        super.deletingAllTasks();
        save();
    }

    @Override
    public void deletingAllEpics() {
        super.deletingAllEpics();
        save();
    }

    @Override
    public void deletingAllsubTasks() {
        super.deletingAllsubTasks();
        save();
    }

    //Получение по идентификатору
    @Override
    public Task getTaskId(int id) {
        Task task = super.getTaskId(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicId(int id) {
        Epic epic = super.getEpicId(id);
        save();
        return epic;
    }

    @Override
    public SubTask getSubTasksId(int id) {
        SubTask subTask = super.getSubTasksId(id);
        save();
        return subTask;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask createdSubTask = super.createSubTask(subTask);
        save();
        return createdSubTask;
    }

    //Обновление
    @Override
    public boolean updateTask(Task task) {
        boolean isUpdateTask = super.updateTask(task);
        if (isUpdateTask) {
            save();
        }
        return isUpdateTask;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isUpdateEpic = super.updateEpic(epic);
        if (isUpdateEpic) {
            save();
        }
        return isUpdateEpic;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean isUpdateSubTask = super.updateSubTask(subTask);
        if (isUpdateSubTask) {
            save();
        }
        return isUpdateSubTask;
    }

    //Удаление по идентификатору
    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    private static String historyToString(List<Task> history) {
        return history.stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(", "));
    }

    private static List<Integer> historyFromString(String value) {
        return Arrays.stream(value.split(", "))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void save() {
        List<Task> allTask = new ArrayList<>();
        allTask.addAll(getListOfTasks());
        allTask.addAll(getListOfEpics());
        allTask.addAll(getListOfSubTasks());
        allTask.sort(Comparator.comparingInt(Task::getId));
        try (FileWriter fileWriter = new FileWriter(file.toString(), StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic,startTime,durationMinutes\n");
            for (Task task : allTask) {
                fileWriter.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s" + "\n",
                        task.getId(),
                        getTaskType(task),
                        task.getName(),
                        task.getStatus(),
                        task.getDescription(),
                        task instanceof SubTask ? String.valueOf(((SubTask) task).getEpicId()) : "",
                        task.getStartTime() != null ? task.getStartTime().format(Managers.formatter) : "",
                        task.getDuration() != null ? task.getDuration() : ""
                ));
            }
            if (!getHistory().isEmpty()) {
                fileWriter.write("\n" + historyToString(getHistory()));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время записи в файл.", e);
        }
    }

    private void fromString(String value) {
        try {
            String[] parts = value.split(",");
            if (parts.length <= 7) {
                throw new IllegalArgumentException("Неверный формат задачи, ожидается 7 полей, получено " + parts.length);
            }
            int id = Integer.parseInt(parts[0]);
            String type = parts[1];
            String name = parts[2];
            TaskStatus status = TaskStatus.valueOf(parts[3]);
            String description = parts[4];
            ZonedDateTime startTime = ZonedDateTime.parse(parts[6].trim(), Managers.formatter);
            Duration durationMinutes = Duration.parse(parts[7].trim());

            switch (type) {
                case "TASK":
                    Task task = createTask(new Task(name, description, status, startTime, durationMinutes.toMinutes()));
                    tasks.put(id, task);
                    break;
                case "EPIC":
                    Epic epic = createEpic(new Epic(name, description));
                    epic.setStatus(status);
                    epics.put(id, epic);
                    break;
                case "SUBTASK":
                    int epicId = Integer.parseInt(parts[5]);
                    SubTask subTask = createSubTask(new SubTask(name, description, status, epicId, startTime, durationMinutes.toMinutes()));
                    subTasks.put(id, subTask);
                    break;
                default:
                    throw new IllegalArgumentException("Тип не существует: " + type);
            }
        } catch (Exception e) {
            throw new ManagerLoadException("Произошла ошибка во время парсинга строки из файла.");
        }
    }

    private int getMaxIdInFile(String[] lines) {
        int maxId = 1;
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                break;
            }
            String[] parts = lines[i].split(",");
            int id = Integer.parseInt(parts[0]);
            maxId = Math.max(maxId, id);
        }
        return maxId;
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof SubTask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }
}
