package core.yaoquan.hanxu.util;

import java.util.Map;

public class Cast {
    public static int toInteger(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value instanceof Number? ((Number) value).intValue() : defaultValue;
    }

    public static float toFloat(Map<String, Object> map, String key, float defaultValue) {
        Object value = map.get(key);
        return value instanceof Number? ((Number) value).floatValue() : defaultValue;
    }

    public static double toDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        return value instanceof Number? ((Number) value).doubleValue() : defaultValue;
    }

    public static String toString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value instanceof String? (String) value : defaultValue;
    }

    public static boolean toBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        return value instanceof Boolean? (Boolean) value : defaultValue;
    }

    public static int toInteger(Object object, int defaultValue) {
        return object instanceof Number? ((Number) object).intValue() : defaultValue;
    }

    public static float toFloat(Object object, float defaultValue) {
        return object instanceof Number? ((Number) object).floatValue() : defaultValue;
    }

    public static double toDouble(Object object, double defaultValue) {
        return object instanceof Number? ((Number) object).doubleValue() : defaultValue;
    }

    public static String toString(Object object, String defaultValue) {
        return object instanceof String? (String) object : defaultValue;
    }

    public static boolean toBoolean(Object object, boolean defaultValue) {
        return object instanceof Boolean? (Boolean) object : defaultValue;
    }
}
