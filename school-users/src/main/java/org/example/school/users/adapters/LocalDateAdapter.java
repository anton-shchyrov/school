package org.example.school.users.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter implements JsonDeserializer<LocalDate>, JsonSerializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return LocalDate.parse(jsonElement.getAsString(), DateTimeFormatter.ISO_DATE);
    }

    @Override
    public JsonElement serialize(LocalDate localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        if (localDateTime == null)
            return JsonNull.INSTANCE;
        return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_DATE));
    }
}