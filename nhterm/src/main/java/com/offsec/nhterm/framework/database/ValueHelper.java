package com.offsec.nhterm.framework.database;

import android.database.Cursor;
import java.lang.reflect.Field;

/**
 * @author kiva
 */
public class ValueHelper {

  /**
   * 根据数据类型将数据库中的值写入到相应的字段.
   *
   * @param cursor   游标
   * @param object   赋值对象
   * @param field    赋值字段
   * @param dataType 数据类型
   */
  public static void setKeyValue(Cursor cursor, Object object, Field field, DatabaseDataType dataType, int index) {
    switch (dataType) {
      case INTEGER:
        try {
          field.set(object, cursor.getInt(index));
        } catch (Throwable e) {
          try {
            //支持Boolean类型
            //因为Boolean默认当Integer处理
            field.set(object, cursor.getInt(index) != 0);
          } catch (IllegalAccessException ignored) {
          }
        }
        break;
      case TEXT:
        try {
          field.set(object, cursor.getString(index));
        } catch (IllegalAccessException ignored) {
        }
        break;
      case FLOAT:
        try {
          field.set(object, cursor.getFloat(index));
        } catch (IllegalAccessException ignored) {
        }
        break;
      case BIGINT:
        try {
          field.set(object, cursor.getLong(index));
        } catch (IllegalAccessException ignored) {
        }
        break;
      case DOUBLE:
        try {
          field.set(object, cursor.getDouble(index));
        } catch (IllegalAccessException ignored) {
        }
        break;

    }
  }

  /**
   * 根据数据类型从字段中提取值并转换为String
   *
   * @param dataType
   * @param field
   * @param o
   * @return
   * @throws IllegalAccessException 无法转换时抛出的异常
   */
  public static String valueToString(DatabaseDataType dataType, Field field, Object o) throws IllegalAccessException {
    switch (dataType) {
      case INTEGER:
        Object f = field.get(o);
        if (f == null) {
          return "NULL";
        }
        if (f instanceof Boolean) {
          return ((Boolean) f) ? "1" : "0";
        }
        if (f instanceof Number) {
          return String.valueOf(((Number) f).intValue());
        }
        return String.valueOf(f);
      case TEXT:
        Object ft = field.get(o);
        if (ft == null) return "NULL";
        return "\"" + ft.toString() + "\"";
      case DOUBLE:
        Object fd = field.get(o);
        if (fd == null) return "NULL";
        if (fd instanceof Number) return String.valueOf(((Number) fd).doubleValue());
        return fd.toString();
      case FLOAT:
        Object ff = field.get(o);
        if (ff == null) return "NULL";
        if (ff instanceof Number) return String.valueOf(((Number) ff).floatValue());
        return ff.toString();
      case BIGINT:
        Object fl = field.get(o);
        if (fl == null) return "NULL";
        if (fl instanceof Number) return String.valueOf(((Number) fl).longValue());
        return fl.toString();
    }
    return null;
  }

  /**
   * 根据数据类型将对象转换为String
   *
   * @param dataType
   * @param o
   * @return
   */
  public static String valueToString(DatabaseDataType dataType, Object o) {
    switch (dataType) {
      case INTEGER:
        if (o == null) return "NULL";
        if (o instanceof Boolean) {
          return ((Boolean) o) ? "1" : "0";
        }
        if (o instanceof Number) {
          return String.valueOf(((Number) o).intValue());
        }
        return String.valueOf(o);
      case TEXT:
        return (o == null) ? "NULL" : "\"" + o + "\"";
      case DOUBLE:
        if (o == null) return "NULL";
        if (o instanceof Number) return String.valueOf(((Number) o).doubleValue());
        return o.toString();
      case FLOAT:
        if (o == null) return "NULL";
        if (o instanceof Number) return String.valueOf(((Number) o).floatValue());
        return o.toString();
      case BIGINT:
        if (o == null) return "NULL";
        if (o instanceof Number) return String.valueOf(((Number) o).longValue());
        return o.toString();
    }
    return null;
  }
}
