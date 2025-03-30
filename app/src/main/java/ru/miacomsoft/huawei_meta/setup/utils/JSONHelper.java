package ru.miacomsoft.huawei_meta.setup.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class JSONHelper {

    public static JSONObject loadJSONFromFile(File file) throws IOException, JSONException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        String jsonString = new String(data, StandardCharsets.UTF_8);
        return new JSONObject(jsonString);
    }

    public static void saveJSONToFile(JSONObject jsonObject, File file) throws IOException {
        String jsonString = jsonObject.toString();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    public static Iterator<String> getKeys(JSONObject jsonObject) {
        return jsonObject.keys();
    }
}