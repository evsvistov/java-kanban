package http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import manager.Managers;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {

    @Override
    public void write(JsonWriter jsonWriter, ZonedDateTime zonedDateTime) throws IOException {
        if (zonedDateTime == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(zonedDateTime.format(Managers.formatter));
        }
    }

    @Override
    public ZonedDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == com.google.gson.stream.JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            String dateTimeString = jsonReader.nextString();
            return ZonedDateTime.parse(dateTimeString, Managers.formatter);
        }
    }

}
