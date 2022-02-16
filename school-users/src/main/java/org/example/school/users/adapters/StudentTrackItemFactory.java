package org.example.school.users.adapters;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.example.school.users.protocol.student.StudentTrackItem;

import java.io.IOException;

public class StudentTrackItemFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!StudentTrackItem.class.isAssignableFrom(typeToken.getRawType()))
            return null;
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
        TypeAdapter<JsonObject> jsonAdapter = gson.getAdapter(JsonObject.class);
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter jsonWriter, T t) throws IOException {
                if (t == null) {
                    jsonWriter.nullValue();
                    return;
                }
                JsonObject obj = delegate.toJsonTree(t).getAsJsonObject();
                obj.add("average", gson.toJsonTree(((StudentTrackItem)t).getAverage()));
                jsonAdapter.write(jsonWriter, obj);
            }

            @Override
            public T read(JsonReader jsonReader) throws IOException {
                return delegate.read(jsonReader);
            }
        };
    }
}