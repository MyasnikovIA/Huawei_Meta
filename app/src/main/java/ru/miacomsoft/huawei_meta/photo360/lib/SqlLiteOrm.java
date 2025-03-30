package ru.miacomsoft.huawei_meta.photo360.lib;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


/**
 *
 *
 *
 *                dbHelper = new SQLLiteORM(this);
 *                try {
 *                     long rowID =dbHelper.insertJson("test",new JSONObject("{\"name\":\""+name+"\",\"email\":\""+email+"\"}"));
 *                     Log.d(LOG_TAG, "row inserted, ID = " + rowID);
 *                 } catch (JSONException e) {
 *                     Log.d(LOG_TAG, "eRROR"+ e.toString());
 *                     //e.printStackTrace();
 *                 }
 *                dbHelper.close();
 *
 *         try {
 *             sqlite = new SQLLiteORM(this);
 *             if (!sqlite.getIsExistTab("config")) {
 *                 JSONObject raw = new JSONObject();
 *                 raw.put("DeviceConnectName", "PowerSW3");
 *                 sqlite.insertJson("config", raw);
 *             }
 *             config = sqlite.getJson("config", 1);
 *             editTextDeviceName.setText(config.getString("DeviceConnectName"));
 *         } catch (JSONException e) {
 *             e.printStackTrace();
 *         }
 *
 *  Самописная ORM для работы с SQLLite
 *
 * //  делаем запрос
 * //  SELECT * FROM (   SELECT * FROM "+ TabName +"  ORDER BY id DESC    ) topfunny LIMIT 1;
 * //  String sqlPole = "SELECT * FROM (   SELECT * FROM " + TabName + "  ORDER BY id DESC    ) topfunny LIMIT 1";
 *
 * @code {
 * SQLLiteORM sqlite = new SQLLiteORM(this); // создаем экземпляр класса
 * sqlite.dropTable("test");             // Удаление таблицы
 * <p>
 * JSONObject raw = new JSONObject();
 * try {
 * raw.put("HostCol", "128.0.24.172");
 * raw.put("PortCol", 8266);
 * raw.put("floatCol", 1.2);
 * raw.put("doubleCol", 1.200001d);
 * sqlite.insertJson("test", raw);
 * } catch (JSONException e) {
 * e.printStackTrace();
 * }
 * <p>
 * JSONArray arr = sqlite.sql("select * from test",null);
 * Log.i("SQL", arr.toString());
 * Log.i("SQL", sqlite.getJson("test",1).toString());
 * <p>
 * <p>
 * JSONObject readJson = sqlite.getJson("test", 1);
 * Log.i("SQL", readJson.toString());
 * <p>
 * sqlite.updateJson("test", readJson);
 * <p>
 * DBHelper sqlite; // Объявляем экземпляр класса
 * sqlite = new DBHelper(context); // создаем экземпляр класса
 * <p/>
 * // Создаем хэш таблицу, по которой будут создана таблица SQLite
 * Hashtable<String, Object> raw2 = new Hashtable<String, Object>(10, (float) 0.5);
 * raw2.put("Host", "212.164.223.134");
 * raw2.put("Port", "7002");
 * raw2.put("NameSpace", "User");
 * raw2.put("User", "_SYSTEM");
 * raw2.put("Pass", "sys");
 * Hashtable<Object, Hashtable<String, Object>> raw2List = new Hashtable<Object,Hashtable<String, Object>>(10, (float) 0.5);
 * <p/>
 * // Добавляем запись в SQList , если таблица несозданна , она создастся по образу этой ХЭШ таблице
 * sqlite.addRaw("Имя таблицы", raw2);
 * <p/>
 * // Добавляем группу записей в SQList , если таблица несозданна , она создастся по образу этой ХЭШ таблице
 * sqlite.addRawList("Имя таблицы", raw2List);
 * <p/>
 * // получить количество записей в таблице
 * int count = sqlite.getCountRaw("Имя таблицы");
 * <p/>
 * // Удалить таблицу
 * sqlite.dropTable("Имя таблицы");
 * <p/>
 * // Удалить все записи в таблице
 * sqlite.delRaw("ConnectConf", "");
 * <p/>
 * // Удалить записи в таблице по условию
 * sqlite.delRaw("ConnectConf", " id=1 ");
 * }
 * Created by Администратор on 06.08.15.
 */
public class SqlLiteOrm extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private Context context;
    private String IdRaw = "";

    public SqlLiteOrm(Context context) {
        super(context, "sqlite.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
            /*
            // создаем таблицу с полями
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "name text,"
                    + "email text" + ");");
                    */
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void dropTable(String TabName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("drop table if exists " + TabName);
            db.close();
        } catch (Exception ex) {
            //   Logger.getLogger(SQLiteProxyAndroid.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Функция создания SQL запроса для создания таблицы
     *
     * @param TabName - имя таблицы
     * @param Tab     - Hashtable<"название поля", "значение"> из "значение"  получаем тип поля
     * @return
     */
    public String createSqlTabJson(String TabName, JSONObject Tab) {
        final StringBuffer sb = new StringBuffer();
        Iterator<String> iterator = Tab.keys();
        int numPole = 0;
        sb.append("create table if not exists " + TabName + " ( ");
        sb.append(" id integer primary key autoincrement,");
        while (iterator.hasNext()) {
            numPole++;
            String key = iterator.next();
            Object value = null;
            try {
                value = Tab.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String dataType = value.getClass().getSimpleName();
            String typ = "TEXT";
            if (dataType.equalsIgnoreCase("Integer")) {
                typ = "INT";
            } else if (dataType.equalsIgnoreCase("Long")) {
                typ = "BIGINT";
            } else if (dataType.equalsIgnoreCase("Float")) {
                typ = "REAL";
            } else if (dataType.equalsIgnoreCase("Double")) {
                typ = "REAL";
                // } else if (dataType.equalsIgnoreCase("Boolean")) {
                //     // придумать решение для хронения boolean значения
                //     typ = " BOOLEAN CHECK (" + key + " IN (0,1,null)";
            } else if (dataType.equalsIgnoreCase("String")) {
                typ = "TEXT";
            }
            if (numPole == 1) {
                sb.append(" " + key + "  " + typ + " ");
            } else {
                sb.append(" , " + key + "  " + typ + " ");
            }
        }
        sb.append(" );");
        //Log.i("SQL", sb.toString());
        return sb.toString();
    }


    /**
     * вставить JSON объект в таблицу
     *
     * @param TabName
     * @param Tab
     * @return
     */
    public long insertJson(String TabName, JSONObject Tab) {
        long ret = -1;
        if (Tab == null){
            return ret;
        }
        // SELECT count(*) FROM sqlite_master WHERE type='table' AND name='table_name';
        try {
            String sql = createSqlTabJson(TabName, Tab);
            // Log.d("MainActivity",sql);
            SQLiteDatabase db = getWritableDatabase();
            // создаем таблицу если она отсутствует
            db.execSQL(sql);
            ret = db.insert(TabName, null, getContentFromHashTabJson(Tab));
        } catch (Exception ex) {
            ret = -1;
        }
        return ret;
    }

    public ContentValues getContentFromHashTabJson(JSONObject Tab) {
        ContentValues values = new ContentValues();
        Iterator<String> iterator = Tab.keys();
        while (iterator.hasNext()) {
            try {
                String key = iterator.next();
                Object value = null;
                value = Tab.get(key);
                String dataType = value.getClass().getSimpleName();
                //Log.i("SQL", "dataType:" + dataType + "  " + key + "=" + Tab.get(key));
                if (dataType.equalsIgnoreCase("Integer")) {
                    values.put((String) key, (Integer) Tab.get(key));
                } else if (dataType.equalsIgnoreCase("Long")) {
                    values.put((String) key, (Long) Tab.get(key));
                } else if (dataType.equalsIgnoreCase("Float")) {
                    values.put((String) key, (Float) Tab.get(key));
                } else if (dataType.equalsIgnoreCase("Double")) {
                    values.put((String) key, Float.valueOf(String.valueOf(Tab.get(key))));
                    //  } else if (dataType.equalsIgnoreCase("Boolean")) {
                    //      // придумать решение для хронения boolean значения
                    //          if (((Boolean) Tab.get(key)) == true) {
                    //              values.put((String) key, 1);
                    //          } else {
                    //              values.put((String) key, 0);
                    //          }
                } else if (dataType.equalsIgnoreCase("String")) {
                    values.put((String) key, String.valueOf(Tab.get(key)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return values;
    }

    /**
     * Проверка существования таблицы в БД
     *
     * @param TabName
     * @return
     */
    public boolean getIsExistTab(String TabName) {
        boolean isExist = false;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE name=\"" + TabName + "\"", null);
        if (cursor.moveToFirst() == true) {
            if (cursor.getCount() > 0) {
                isExist = true;
            } else {
                isExist = false;
            }
        } else {
            isExist = false;
        }
        cursor.close();
        return isExist;
    }
    /**
     * Получаем количество записей в таблице
     *
     * @param TabName - имя таблицы
     * @return
     */
    public int getCountRaw(String TabName) {
        if (getIsExistTab(TabName) == false) {
            return -1;
        }
        int count = -1;
        try {

            SQLiteDatabase db = getWritableDatabase();
            Cursor cursor = db.rawQuery("select count(*) from " + TabName, null);
            if (cursor.moveToFirst() == true) {
                count = cursor.getInt(0);
            } else {
                count = -1;
            }
        } catch (Exception e) {
            count = -1;
        }
        return count;
    }
    /**
     * Получить список объектов из курсора ХЭШ
     *
     * @param cursor
     * @return
     */
    public JSONArray getHashTabFromCursorEny(Cursor cursor) {
        final JSONArray rawListres = new JSONArray();
        String[] colNames = cursor.getColumnNames();
        int colnum = colNames.length;
        int numRec = 0;
        while (cursor.moveToNext()) {
            IdRaw = cursor.getString(1);
            final JSONObject rawRes = new JSONObject();
            numRec++;
            for (int i = 0; i < colnum; i++) {
                // Log.d("myLogs", cursor.getType(i) + "  " + colNames[i] + "=" + cursor.getString(i));
                try {
                    rawRes.put(colNames[i], cursor.getString(i).replace(".", ","));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    // 1 - int
                    if (Integer.valueOf(cursor.getType(i)) == 1) {
                        rawRes.put(colNames[i], cursor.getInt(i));
                    } else if (Integer.valueOf(cursor.getType(i)) == 3) {                 // 3 - String
                        rawRes.put(colNames[i], cursor.getString(i));
                    } else if (Integer.valueOf(cursor.getType(i)) == 2) { // 2 - Double
                        rawRes.put(colNames[i], cursor.getFloat(i));
                        // Log.i("SQL", cursor.getType(i) + "  " + colNames[i] + "=" + Double.valueOf(cursor.getString(i)));
                    } else {
                        rawRes.put(colNames[i], cursor.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            rawListres.put(rawRes);
            //   tv.append(numRec + "\r\n");
        }
        return rawListres;
    }
    /***
     *  Получить строки из SQL запроса
     * @param sqlQuery - текст запроса
     * @param selectionArgs - Входящие аргумены
     * @return
     */
    public JSONArray sql(String sqlQuery, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, selectionArgs);
        return getHashTabFromCursorEny(cursor);
    }

    public boolean updateJson(String TabName, JSONObject Tab) {
        boolean ret = false;
        // SELECT count(*) FROM sqlite_master WHERE type='table' AND name='table_name';
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = getContentFromHashTabJson(Tab);
            //  внести изменения во все записи
            IdRaw = String.valueOf(db.update(TabName, cv, null, null));
            cv.clear();
            ret = true;
        } catch (Exception ex) {
            ret = false;
        }
        return ret;
    }

    /***
     * Удаление записи в таблице
     * @param tabName
     * @param id
     * @return
     */
    public boolean del(String tabName, int id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String sql = "DELETE FROM " + tabName + " WHERE id = " + id;
            Cursor cursor = db.rawQuery(sql, null);
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /***
     * Получить JSON объект из таблици по ID
     * @param TabName
     * @param id
     * @return
     */
    public JSONObject getJson(String TabName, long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TabName + " where id=" + id, null);
        final JSONArray tmp = getHashTabFromCursorEny(cursor);
        if (tmp.length() > 0) {
            try {
                return (JSONObject) tmp.get(0);
            } catch (JSONException e) {
                return null;
            }
        }else{
            try {
                return new JSONObject("{}");
            } catch (JSONException e) {
                return null;
            }
        }
    }

    public JSONArray getRows(String tableName, String where) {
        String whereText = "";
        if (where.length()>0) {
            whereText = " where "+where;
        }
        try {
            return sql("select * from "+tableName+whereText,null);
        } catch (Exception e) {
            // System.out.println(e.getMessage());
            return null;
        }
    }

    /***
     * Очистить таблицу
     * @param tableName
     * @return
     */
    public int clearRow(String tableName) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.delete(tableName, null, null);
        } catch (Exception e) {
            return -1;
        }
    }
}
