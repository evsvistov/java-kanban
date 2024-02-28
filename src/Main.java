import Manager.*;
import Task.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        //Добавляем задания для проверки и сразу создаём Задачи
        Task task1 = new Task("Таска 1", "Описание таски 1", TaskStatus.IN_PROGRESS);
        taskManager.createTask(task1);

        Task task2 = new Task("Таска 2", "Описание таски 2", TaskStatus.NEW);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 1");
        taskManager.createEpic(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
        taskManager.createSubTask(subTask1);
        //epic1.addSubtask(subTask1.getId()); //добавляем сабтаск к эпику

        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epic1.getId());
        taskManager.createSubTask(subTask2);
        //epic1.addSubtask(subTask2.getId()); //добавляем сабтаск к эпику

        SubTask subTask3 = new SubTask("Подзадача 3", "Описание подзадачи 3", TaskStatus.DONE, epic2.getId());
        taskManager.createSubTask(subTask3);
        //epic2.addSubtask(subTask3.getId()); //добавляем сабтаск к эпику

        SubTask subTask4 = new SubTask("Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW, epic2.getId());
        taskManager.createSubTask(subTask4);
        //epic2.addSubtask(subTask4.getId()); //добавляем сабтаск к эпику

        //Получение списка всех задач
        printAllTasks(taskManager);

        System.out.println("Получение по идентификатору");
        System.out.println(taskManager.getEpicId(epic1.getId()));
        System.out.println(taskManager.getTaskId(task2.getId()));
        System.out.println(taskManager.getSubTasksId(subTask3.getId()));

        //добавить более 10 вызовов

        System.out.println("Обновление");
        Epic epic3 = new Epic("New Epic", "New Description");
        epic3.setId(epic2.getId());
        System.out.println(taskManager.updateEpic(epic3));
        SubTask subTask5 = new SubTask("New SubTask", "New Description", TaskStatus.DONE, epic2.getId());
        subTask5.setId(subTask3.getId());
        System.out.println(taskManager.updateSubTask(subTask5));

        //System.out.println(taskManager.getTaskId(subTask1.getId()));
        //System.out.println(taskManager.getTaskId(epic1.getId()));

        System.out.println("Удаление по ID таски");
        taskManager.deleteTask(task1.getId());
        //System.out.println("Удаление по ID эпика");
        taskManager.deleteEpic(epic1.getId());
        System.out.println("Удаление по ID сабтаски");
        taskManager.deleteSubTask(subTask4.getId());

        //Получение списка всех задач
        printAllTasks(taskManager);

        System.out.println("Удаление всех задач");
        //taskManager.deletingAllTasks();
        taskManager.deletingAllsubTasks();
        //taskManager.deletingAllEpics();

        //Получение списка всех задач
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
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
