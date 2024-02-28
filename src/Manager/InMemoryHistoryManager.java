package Manager;

import Task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final int historySize = 10;
    private static final List<Task> taskHistory = new ArrayList<>(10);

    @Override
    public void add(Task task) {
        if (taskHistory.size() >= historySize) {
            taskHistory.remove(0);
        }
        taskHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }
}
