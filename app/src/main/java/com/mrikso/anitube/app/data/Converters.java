package com.mrikso.anitube.app.data;

import androidx.room.TypeConverter;

import com.mrikso.anitube.app.model.SimpleModel;

import java.util.Date;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromSimpleModel(SimpleModel simpleModel) {
        return simpleModel == null ? null : simpleModel.getText();
    }

    @TypeConverter
    public static SimpleModel toSimpleModel(String name) {
        return name == null ? null : new SimpleModel(name, null);
    }
}
