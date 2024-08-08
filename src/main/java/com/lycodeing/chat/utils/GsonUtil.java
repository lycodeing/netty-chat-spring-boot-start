package com.lycodeing.chat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtil {
    private final static Gson GSON = new GsonBuilder().create();


    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }
}
