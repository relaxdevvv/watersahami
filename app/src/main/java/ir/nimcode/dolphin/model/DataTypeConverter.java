package ir.nimcode.dolphin.model;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.Map;

/**
 * Created by saeed on 2/17/18.
 */

public class DataTypeConverter {

    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLong(Date value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public static Map<Long, PropertyValues> toMap(String value) {
        return new Gson().fromJson(value, new TypeToken<Map<Long, PropertyValues>>() {
        }.getType());
    }

    @TypeConverter
    public static String toJson(Map<Long, PropertyValues> value) {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(value);
    }
}