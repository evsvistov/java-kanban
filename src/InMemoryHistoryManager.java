import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_SIZE = 10;
    private static final List<Task> taskHistory = new ArrayList<>(HISTORY_SIZE);

    @Override
    public void add(Task task) {
        if (taskHistory.size() >= HISTORY_SIZE) {
            taskHistory.remove(0);
        }
        taskHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }
}
