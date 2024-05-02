package http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import enums.TaskStatus;

import java.io.IOException;

public class TaskStatusAdapter extends TypeAdapter<TaskStatus> {
    @Override
    public void write(JsonWriter jsonWriter, TaskStatus taskStatus) throws IOException {
        jsonWriter.value(taskStatus.name());
    }

    @Override
    public TaskStatus read(JsonReader jsonReader) throws IOException {
        return TaskStatus.valueOf(jsonReader.nextString());
    }
}
