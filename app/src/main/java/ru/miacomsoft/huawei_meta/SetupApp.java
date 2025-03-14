package ru.miacomsoft.huawei_meta;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SetupApp extends AppCompatActivity {

    public static final int REQUEST_CODE_SETUP_APP = 32001;

    private LinearLayout layoutContainer;
    private Button btnSave;
    private JSONObject jsonObject;

    private Spinner storeSpinner;
    private JSONArray storeArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_point);
        // Перевернуть ориентацию приложения
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        layoutContainer = findViewById(R.id.layoutContainerSetup);
        btnSave = findViewById(R.id.btnSave);

        // Инициализация JSON
        initializeJSON();

        // Генерация UI
        generateUIFromJSON();

        // Сохранение JSON
        btnSave.setOnClickListener(v -> saveJSONToFile());
    }

    private void initializeJSON() {
        File file = new File(getCacheDir(), "setup.json");
        if (file.exists()) {
            // Если файл существует, загружаем его
            try {
                jsonObject = JSONHelper.loadJSONFromFile(file);
            } catch (IOException | JSONException e) {
                Log.e("JSONError", "Error loading JSON from file", e);
                jsonObject = createDefaultJSON();
            }
        } else {
            // Если файла нет, создаем JSON по умолчанию
            jsonObject = createDefaultJSON();
            try {
                JSONHelper.saveJSONToFile(jsonObject, file);
            } catch (IOException e) {
                Log.e("JSONError", "Error saving JSON to file", e);
            }
        }
    }

    private JSONObject createDefaultJSON() {
        JSONObject defaultJSON = new JSONObject();
        try {
            defaultJSON.put("name", "John Doe");
            defaultJSON.put("age", 30);
            defaultJSON.put("isStudent", false);

            JSONObject comboBoxJSON_1 = new JSONObject();
            comboBoxJSON_1.put("name", "Store1");
            comboBoxJSON_1.put("value", "/store/0/ssd");
            comboBoxJSON_1.put("isSeletedRow", true);

            JSONObject comboBoxJSON_2 = new JSONObject();
            comboBoxJSON_2.put("name", "Store2");
            comboBoxJSON_2.put("value", "/store/0/ssd_local2");
            comboBoxJSON_2.put("isSeletedRow", false);

            JSONArray storeArray = new JSONArray();
            storeArray.put(comboBoxJSON_1);
            storeArray.put(comboBoxJSON_2);

            defaultJSON.put("Select store", storeArray);
        } catch (JSONException e) {
            Log.e("JSONError", "Error creating default JSON", e);
        }
        return defaultJSON;
    }

    private void generateUIFromJSON() {
        if (layoutContainer !=null) {
            layoutContainer.removeAllViews(); // Очистка контейнера
        }
        try {
            for (Iterator<String> it = JSONHelper.getKeys(jsonObject); it.hasNext(); ) {
                String key = it.next();
                Object value = jsonObject.get(key);

                // TextView для ключа
                TextView textViewKey = new TextView(this);
                textViewKey.setText(key);
                layoutContainer.addView(textViewKey);
                // Генерация UI в зависимости от типа значения
                if (value instanceof String) {
                    addEditText(key, (String) value);
                } else if (value instanceof Integer) {
                    addEditText(key, String.valueOf(value));
                } else if (value instanceof Boolean) {
                    addCheckBox(key, (Boolean) value);
                } else if (value instanceof JSONArray) {
                    if (key.equals("Select store")) {
                        storeArray = (JSONArray) value;
                        addSpinner(key, storeArray);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSONError", "Error generating UI from JSON", e);
        }
    }

    private void addEditText(String key, String value) {
        androidx.appcompat.widget.AppCompatEditText editText = new androidx.appcompat.widget.AppCompatEditText(this);
        editText.setTag(key); // Сохраняем ключ в теге
        editText.setText(value);
        layoutContainer.addView(editText);
    }

    private void addCheckBox(String key, boolean value) {
        androidx.appcompat.widget.AppCompatCheckBox checkBox = new androidx.appcompat.widget.AppCompatCheckBox(this);
        checkBox.setTag(key); // Сохраняем ключ в теге
        checkBox.setChecked(value);
        layoutContainer.addView(checkBox);
    }

    private void addSpinner(String key, JSONArray storeArray) {
        storeSpinner = new Spinner(this);
        List<String> storeNames = new ArrayList<>();
        int selectedPosition = 0;

        try {
            for (int i = 0; i < storeArray.length(); i++) {
                JSONObject store = storeArray.getJSONObject(i);
                storeNames.add(store.getString("name"));
                if (store.getBoolean("isSeletedRow")) {
                    selectedPosition = i;
                }
            }
        } catch (JSONException e) {
            Log.e("JSONError", "Error parsing store array", e);
        }

        // Адаптер для Spinner
        StoreSpinnerAdapter adapter = new StoreSpinnerAdapter(this, storeNames);
        storeSpinner.setAdapter(adapter);
        storeSpinner.setSelection(selectedPosition);

        // Обработчик выбора элемента в Spinner
        storeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateSelectedStore(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ничего не делаем
            }
        });

        layoutContainer.addView(storeSpinner);
    }

    private void updateSelectedStore(int selectedPosition) {
        try {
            for (int i = 0; i < storeArray.length(); i++) {
                JSONObject store = storeArray.getJSONObject(i);
                store.put("isSeletedRow", i == selectedPosition);
            }
        } catch (JSONException e) {
            Log.e("JSONError", "Error updating selected store", e);
        }
    }

    private void saveJSONToFile() {
        try {
            // Обновляем JSONObject на основе UI
            for (int i = 0; i < layoutContainer.getChildCount(); i++) {
                View view = layoutContainer.getChildAt(i);
                if (view instanceof androidx.appcompat.widget.AppCompatEditText) {
                    androidx.appcompat.widget.AppCompatEditText editText = (androidx.appcompat.widget.AppCompatEditText) view;
                    String key = (String) editText.getTag();
                    String value = editText.getText().toString();
                    jsonObject.put(key, value);
                } else if (view instanceof androidx.appcompat.widget.AppCompatCheckBox) {
                    androidx.appcompat.widget.AppCompatCheckBox checkBox = (androidx.appcompat.widget.AppCompatCheckBox) view;
                    String key = (String) checkBox.getTag();
                    boolean value = checkBox.isChecked();
                    jsonObject.put(key, value);
                }
            }
            // Сохраняем JSON в файл
            File file = new File(getCacheDir(), "setup.json");
            JSONHelper.saveJSONToFile(jsonObject, file);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACTION","RELOAD_APP");
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (JSONException | IOException e) {
            Log.e("JSONError", "Error saving JSON", e);
        }
    }
}