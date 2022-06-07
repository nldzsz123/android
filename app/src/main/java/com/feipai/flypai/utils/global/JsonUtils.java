package com.feipai.flypai.utils.global;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * json工具类
 */
public class JsonUtils {

    private JsonUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * 用来将JSON串转为对象，但此方法不可用来转带泛型的集合
     */
    public static <T> T object(String json, Class<T> classOfT) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将对象转为JSON串
     */
    public static String toJson(Object object) {
        if (object == null) {
            return gson.toJson(JsonNull.INSTANCE);
        }
        return gson.toJson(object);
    }

    /**
     * 用来将JSON串转为对象，此方法可用来转带泛型的集合，如：Type为 new
     * TypeToken<List<T>>(){}.getType()，其它类也可以用此方法调用，就是将List<T>替换为你想要转成的类
     */
    public static Object fromJson(String json, Type typeOfT) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isGoodJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonSyntaxException e) {
        } catch (JsonParseException e) {
        }
        return false;
    }

    public static JsonElement getValueByKey(String json, String key) {
        if (!StringUtils.isEmpty(json)) {
            try {
                JsonElement jsonElement = new JsonParser().parse(json);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement object = jsonObject.get(key);
                return object;
            } catch (JsonSyntaxException e) {
            } catch (JsonParseException e) {
            }
        }
        return null;
    }

}
