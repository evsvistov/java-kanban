import Manager.*;
import Task.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();

        // Создание двух задач
        Task task1 = taskManager.createTask(new Task("Task 1", "Task 1 Description", TaskStatus.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Task 2 Description", TaskStatus.NEW));

        // Создание эпика с тремя подзадачами
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Epic 1 Description"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("SubTask 1", "SubTask 1 Description", TaskStatus.NEW, epic1.getId()));
        SubTask subTask2 = taskManager.createSubTask(new SubTask("SubTask 2", "SubTask 2 Description", TaskStatus.IN_PROGRESS, epic1.getId()));
        SubTask subTask3 = taskManager.createSubTask(new SubTask("SubTask 3", "SubTask 3 Description", TaskStatus.DONE, epic1.getId()));

        // Создание эпика без подзадач
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Epic 2 Description"));

        // Получение списка всех задач
        printAllTasks(taskManager);

        // Запросы задач в разном порядке
        taskManager.getTaskId(task1.getId());
        taskManager.getEpicId(epic1.getId());
        taskManager.getSubTasksId(subTask1.getId());
        taskManager.getTaskId(task2.getId());
        taskManager.getSubTasksId(subTask3.getId());
        taskManager.getSubTasksId(subTask2.getId());
        taskManager.getEpicId(epic2.getId());

        // Повторно запросили
        taskManager.getTaskId(task1.getId());
        taskManager.getEpicId(epic1.getId());

        // Получение списка всех задач
        printAllTasks(taskManager);

        // Удаление задачи
        System.out.println("Удалили задачу c taskId=" + task1.getId());
        taskManager.deleteTask(task1.getId());

        // Получение списка всех задач
        printAllTasks(taskManager);

        // Удаление эпика
        System.out.println("Удаление эпика с подзадачами epicId=" + epic1.getId());
        taskManager.deleteEpic(epic1.getId());

        // Получение списка всех задач
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nЗадачи:");
        for (Task task : manager.getListOfTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getListOfEpics()) {
            System.out.println(epic);

            for (Task task : manager.getListSubTaskOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getListOfSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

}
