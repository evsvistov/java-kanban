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
import java.util.*;

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
            boolean isHistory = false;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) {
                    isHistory = true;
                    continue;
                }
                if (!isHistory) {
                    Task task = fromString(lines[i]);
                    if (task == null) {
                        throw new ManagerLoadException("Произошла ошибка во время парсинга строки из файла.");
                    }
                } else {
                    List<Integer> historyList = historyFromString(lines[i]);
                    Task task;
                    if (!historyList.isEmpty()) {
                        for (Integer taskId : historyList) {
                            task = getTaskId(taskId);
                            task = getEpicId(taskId);
                            task = getSubTasksId(taskId);
                            if (task != null) {
                                fileBackedTaskManager.getHistoryManager().add(task);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Произошла ошибка при чтении файла", e);
        }
        return fileBackedTaskManager;
    }

    public List<Task> getHistoryManager() {
        return super.getHistory();
    }

    public static String historyToString(List<Task> history) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            builder.append(history.get(i).getId());
            if (i < history.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        String[] idTasks = value.split(", ");
        for (String id : idTasks) {
            historyIds.add(Integer.parseInt(id));
        }
        return historyIds;
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
        SubTask task = super.getSubTasksId(id);
        save();
        return task;
    }

    //создание задач
    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
        return subTask;
    }

    //Обновление
    @Override
    public boolean updateTask(Task task) {
        boolean isUpdateTask = super.updateTask(task);
        save();
        return isUpdateTask;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isUpdateEpic = super.updateEpic(epic);
        save();
        return isUpdateEpic;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean isUpdateSubTask = super.updateSubTask(subTask);
        save();
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

    private void save() {
        List<Task> allTask = new ArrayList<>();
        allTask.addAll(getListOfTasks());
        allTask.addAll(getListOfEpics());
        allTask.addAll(getListOfSubTasks());
        allTask.sort(Comparator.comparingInt(Task::getId));
        try (FileWriter fileWriter = new FileWriter(file.toString(), StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : allTask) {
                fileWriter.write(String.format("%d,%s,%s,%s,%s,%s" + "\n",
                        task.getId(),
                        getTaskType(task),
                        task.getName(),
                        task.getStatus(),
                        task.getDescription(),
                        task instanceof SubTask ? ((SubTask) task).getEpicId() : "")
                );
            }
            fileWriter.write("\n" + historyToString(getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка во время записи в файл.", e);
        }
    }

    private Task fromString(String value) {
        Task task;
        String[] parts = value.split(",");
        if (parts.length < 4) {
            throw new IllegalArgumentException("Неверный формат задачи");
        }
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts.length > 4 ? parts[4] : "";
        Integer epicId = parts.length > 5 ? Integer.valueOf(parts[5]) : null;
        switch (type) {
            case "TASK":
                task = createTask(new Task(name, description, status));
                task.setId(id);
                break;
            case "EPIC":
                task = createEpic(new Epic(name, description));
                task.setId(id);
                break;
            case "SUBTASK":
                task = createSubTask(new SubTask(name, description, status, epicId));
                task.setId(id);
                break;
            default:
                throw new IllegalArgumentException("Тип не существует: " + type);
        }
        return task;
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
