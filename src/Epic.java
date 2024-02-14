import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTasks;
    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        subTasks = new ArrayList<>();
    }
    public ArrayList<Integer> getSubtasks() {
        return subTasks;
    }

    public void addSubtask(int subTaskId) {
        subTasks.add(subTaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subTasks +
                '}';
    }
}

