package Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final ArrayList<Integer> subTasks;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        subTasks = new ArrayList<>();
    }

    public List<Integer> getSubtasks() {
        return subTasks;
    }

    public void addSubtask(int subTaskId) {
        subTasks.add(subTaskId);
    }

    public void deleteSubtaskId(int subTaskId) {
        subTasks.remove((Integer) subTaskId);
    }

    public void deleteAllSubtask() {
        subTasks.clear();
    }

    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subTasks +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        Epic epic = (Epic) object;
        return Objects.equals(subTasks, epic.subTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasks);
    }
}

