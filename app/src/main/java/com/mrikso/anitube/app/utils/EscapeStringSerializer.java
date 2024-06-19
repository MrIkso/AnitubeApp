package com.mrikso.anitube.app.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class EscapeStringSerializer implements JsonSerializer<String> {
    @Override
    public JsonElement serialize(String s, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(escapeSpecialChar(s));
    }

    public static String escapeSpecialChar(String string) {
        String[][] escapes = new String[][]{
                {"\\", "\\\\"},
                {"\"", "\\\""},
                {"'", "\\'"},
                {"\n", "\\n"},
                {"\r", "\\r"},
                {"\b", "\\b"},
                {"\f", "\\f"},
                {"\t", "\\t"}
        };
        for (String[] esc : escapes) {
            string = string.replace(esc[0], esc[1]);
        }
        return string;
    }
}
