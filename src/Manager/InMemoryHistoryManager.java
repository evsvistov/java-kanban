package Manager;

import Task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> taskHistory = new HashMap<>();
    private Node head = null;
    private Node tail = null;

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private void linkLast(Node node) {
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }

    @Override
    public void add(Task task) {
        int taskId = task.getId();
        if (taskHistory.containsKey(taskId)) {
            removeNode(taskHistory.get(taskId));
        }
        Node newNode = new Node(task);
        linkLast(newNode);
        taskHistory.put(taskId, newNode);
    }

    @Override
    public void remove(int id) {
        if (taskHistory.containsKey(id)) {
            removeNode(taskHistory.get(id));
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        node.next = null;
        node.prev = null;
        taskHistory.remove(node.task.getId());
    }

    private List<Task> getTasks() {
        List<Task> allTasks = new ArrayList<>();
        Node current = head;
        while (current != null) {
            allTasks.add(current.task);
            current = current.next;
        }
        return allTasks;
    }

}
