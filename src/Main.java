import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

        //Добавляем задания для проверки и сразу создаём Задачи
        Task task1 = new Task("Таска 1", "Описание таски 1", TaskStatus.IN_PROGRESS);
        taskManager.createTask(task1);

        Task task2 = new Task("Таска 2", "Описание таски 2", TaskStatus.NEW);
        taskManager.createTask(task2);

        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createTask(epic1);

        Epic epic2 = new Epic("Эпик 2", "Описание эпика 1");
        taskManager.createTask(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic1.getId());
        taskManager.createTask(subTask1);
        epic1.addSubtask(subTask1.getId()); //добавляем сабтаск к эпику

        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epic1.getId());
        taskManager.createTask(subTask2);
        epic1.addSubtask(subTask2.getId()); //добавляем сабтаск к эпику

        SubTask subTask3 = new SubTask("Подзадача 3", "Описание подзадачи 3", TaskStatus.DONE, epic2.getId());
        taskManager.createTask(subTask3);
        epic2.addSubtask(subTask3.getId()); //добавляем сабтаск к эпику

        SubTask subTask4 = new SubTask("Подзадача 4", "Описание подзадачи 4", TaskStatus.NEW, epic2.getId());
        taskManager.createTask(subTask4);
        epic2.addSubtask(subTask4.getId()); //добавляем сабтаск к эпику

        System.out.println("Получение списка всех задач");
        System.out.println((taskManager.getListOfTasks()));
        System.out.println((taskManager.getListOfEpics()));
        System.out.println((taskManager.getListOfSubTasks()));

        System.out.println("Получение по идентификатору");
        System.out.println(taskManager.getEpicId(epic1.getId()));
        System.out.println(taskManager.getTaskId(task2.getId()));
        System.out.println(taskManager.getSubTasksId(subTask3.getId()));

        System.out.println("Получение сабтасок эпика по ID эпика");
        System.out.println(taskManager.getListSubTaskOfEpic(epic1.getId()));

        System.out.println("Обновление");
        task1.setName("new_Таска 1");
        System.out.println(taskManager.updateTask(task1));
        System.out.println(taskManager.updateTask(subTask1));

        //System.out.println(taskManager.getTaskId(subTask1.getId()));
        //System.out.println(taskManager.getTaskId(epic1.getId()));

        System.out.println("Удаление по ID таски");
        taskManager.deleteTask(task1.getId());
        System.out.println("Удаление по ID эпика");
        taskManager.deleteEpic(epic1.getId());
        System.out.println("Удаление по ID сабтаски");
        taskManager.deleteSubTask(subTask4.getId());

        System.out.println("Получение сабтасок эпика по ID эпика");
        System.out.println(taskManager.getListSubTaskOfEpic(epic2.getId()));

        System.out.println("Получение списка всех задач");
        System.out.println((taskManager.getListOfTasks()));
        System.out.println((taskManager.getListOfEpics()));
        System.out.println((taskManager.getListOfSubTasks()));

        System.out.println("Удаление всех задач");
        taskManager.deletingAllTasks();
        taskManager.deletingAllsubTasks();
        taskManager.deletingAllEpics();

        System.out.println("Получение списка всех задач");
        System.out.println((taskManager.getListOfTasks()));
        System.out.println((taskManager.getListOfEpics()));
        System.out.println((taskManager.getListOfSubTasks()));

    }

}
