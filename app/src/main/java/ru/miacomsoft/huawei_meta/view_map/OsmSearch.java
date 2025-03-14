package ru.miacomsoft.huawei_meta.view_map;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class OsmSearch {

    /**
     * Интерфейс для обработки ответов от сервера.
     */
    public interface CallbackAnswer {
        void call(JSONObject answer);
    }

    private String errorText;

    public static void query(String queryText,CallbackAnswer callback) {
        // https://nominatim.openstreetmap.org/search?format=json&q=%D0%BF%D0%B0%D1%88%D0%B8%D0%BD%D0%BE
        OsmSearch osmSearch = new OsmSearch();
        osmSearch.postQuery("https://nominatim.openstreetmap.org/search?format=json&q="+queryText,new JSONObject(),callback);
    }

    /**
     * Отправляет запрос на сервер.
     * @param pathUrl Путь запроса
     * @param inputParam Параметры запроса
     * @param callback Коллбэк для обработки ответа
     * @return Ответ от сервера
     */
    private JSONObject postQuery(String pathUrl, JSONObject inputParam, CallbackAnswer callback){
        JSONObject result = new JSONObject();
        String jsonInputString = inputParam != null ? inputParam.toString() : null;
        try {
            URL url = new URL(pathUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("sec-ch-ua-platform", "Android");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Infinix X6833B Build/UP1A.231005.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/133.0.6943.137 Mobile Safari/537.36");
            connection.setRequestProperty("sec-ch-ua", "\"Not(A:Brand\";v=\"99\", \"Android WebView\";v=\"133\", \"Chromium\";v=\"133\"");
            connection.setRequestProperty("sec-ch-ua-mobile", "?1");
            connection.setDoOutput(true);
            if (jsonInputString != null && !jsonInputString.equals("{}")) {
                connection.setRequestMethod("POST");
                OutputStream os = connection.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            if (connection.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        content.append(responseLine);
                    }
                    if (content.length() > 0 && content.charAt(0) == '{' && content.charAt(content.length() - 1) == '}') {
                        result = new JSONObject(content.toString());
                        if (callback != null) callback.call(result);
                    }
                }
            }
            connection.disconnect();
        } catch (IOException | JSONException e) {
            errorText = formatException(e);
            try {
                result.put("error", errorText);
            } catch (JSONException ex) {
                throw new RuntimeException(ex);
            }
            if (callback != null) callback.call(result);
        }
        return result;
    }
    /**
     * Форматирует исключение в строку.
     * @param exception Исключение
     * @return Форматированная строка с ошибкой
     */
    public static String formatException(Exception exception) {
        StringBuilder sbError = new StringBuilder();
        sbError.append("\nПроизошла ошибка: ").append(exception.getMessage());
        sbError.append("\nТип ошибки: ").append(exception.getClass().getName());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        sbError.append("\nПодробное описание:\n").append(sw.toString());
        int index = 0;
        for (StackTraceElement element : exception.getStackTrace()) {
            sbError.append("\n").append(++index).append(")\nLine number:")
                    .append(element.getLineNumber()).append("\nClassName:")
                    .append(element.getClassName()).append("\nFileName:")
                    .append(element.getFileName()).append("\n")
                    .append(element.toString());
        }
        return sbError.toString();
    }
}
