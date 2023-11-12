package com.com2vio.utils;

import com.com2vio.entities.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import java.time.LocalDateTime;

public class DataUtils {

    public static final Gson LOCAL_GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
        .create();

    public static String extractUser(JSONObject userJson) {
        if (userJson == null) {
            return null;
        }
        User user = new User();
        user.setId(userJson.optString("id"));
        user.setNodeId(userJson.optString("node_id"));
        user.setUrl(userJson.optString("url"));
        return LOCAL_GSON.toJson(user);
    }
}
