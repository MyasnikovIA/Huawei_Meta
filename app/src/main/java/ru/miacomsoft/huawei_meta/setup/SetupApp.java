package ru.miacomsoft.huawei_meta.setup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.miacomsoft.huawei_meta.R;
import ru.miacomsoft.huawei_meta.setup.utils.JSONHelper;


public class SetupApp extends AppCompatActivity {
    public interface CallbackEmptyReturn {
        void call();
    }
    public static final int REQUEST_CODE_SETUP_APP = 32001;
    public static JSONObject LOCAL_STORAGE_APP = new JSONObject();
    public static JSONObject CONFIG = new JSONObject();

    private LinearLayout layoutContainer;
    private Button btnSave;
    private JSONObject jsonObject;

    private Spinner storeSpinner;
    private JSONArray storeArray;
    private static String fileNameSetup = "setup.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_app);

        // Перевернуть ориентацию приложения
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        layoutContainer = findViewById(R.id.layoutContainerSetup);
        btnSave = findViewById(R.id.btnSave);
        initializeJSON();
        generateUIFromJSON();
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

    private static JSONObject createDefaultJSON() {
        JSONObject defaultJSON = new JSONObject();
        try {
            JSONObject observerProperty = new JSONObject();
            observerProperty.put("value", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/CV60");
            observerProperty.put("type", "string");
            observerProperty.put("text", "Tracking directory");
            defaultJSON.put("observer", observerProperty);

            JSONObject isRunServiceProperty = new JSONObject();
            isRunServiceProperty.put("value", false);
            isRunServiceProperty.put("type", "boolean");
            isRunServiceProperty.put("text", "Delete original photo");
            defaultJSON.put("remuveSrcPhoto", isRunServiceProperty);

            JSONObject isFlipTheScreen = new JSONObject();
            isFlipTheScreen.put("value", false);
            isFlipTheScreen.put("type", "boolean");
            isFlipTheScreen.put("text", "Flip the screen");
            defaultJSON.put("FlipTheScreen", isFlipTheScreen);

            JSONObject projectProperty = new JSONObject();
            projectProperty.put("value", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/HUAWEI_META");
            projectProperty.put("type", "string");
            projectProperty.put("text", "Project directory");
            defaultJSON.put("projectDir", projectProperty);

            JSONObject projectMapTmpDir = new JSONObject();
            projectMapTmpDir.put("value", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/MAP_IMG");
            projectMapTmpDir.put("type", "string");
            projectMapTmpDir.put("text", "Map image temp directory");
            defaultJSON.put("MapTmpDir", projectMapTmpDir);

            JSONObject packetNameCam = new JSONObject();
            packetNameCam.put("value", "com.huawei.cvIntl60");
            packetNameCam.put("type", "string");
            packetNameCam.put("text", "Packet name camera");
            defaultJSON.put("PacketNameCamera", packetNameCam);


//            выпадающий список combobox
//            JSONObject comboBoxJSON_1 = new JSONObject();
//            comboBoxJSON_1.put("name", "Store1");
//            comboBoxJSON_1.put("value", "/store/0/ssd");
//            comboBoxJSON_1.put("type", "string");
//            comboBoxJSON_1.put("text", "Внешний носитель информации");
//            comboBoxJSON_1.put("isSeletedRow", true);
//
//            JSONObject comboBoxJSON_2 = new JSONObject();
//            comboBoxJSON_2.put("name", "Store2");
//            comboBoxJSON_2.put("value", "/store/0/ssd_local2");
//            comboBoxJSON_2.put("type", "string");
//            comboBoxJSON_2.put("text", "Внутренний носитель информации");
//            comboBoxJSON_2.put("isSeletedRow", false);
//
//            JSONArray storeArray = new JSONArray();
//            storeArray.put(comboBoxJSON_1);
//            storeArray.put(comboBoxJSON_2);
//            defaultJSON.put("Select_store", storeArray);

        } catch (JSONException e) {
            Log.e("JSONError", "Error creating default JSON", e);
        }
        return defaultJSON;
    }

    private void generateUIFromJSON() {
        layoutContainer.removeAllViews(); // Очистка контейнера

        try {
            for (Iterator<String> it = JSONHelper.getKeys(jsonObject); it.hasNext(); ) {
                String key = it.next();
                Object value = jsonObject.get(key);

                // Создаем горизонтальный LinearLayout для каждой строки атрибута
                LinearLayout rowLayout = new LinearLayout(this);
                //rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setOrientation(LinearLayout.VERTICAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                // TextView для текста атрибута (поле "text")
                TextView textViewKey = new TextView(this);
                String valStr = getTextForAttribute(value);
                if (valStr.length()>0) {
                    textViewKey.setText(valStr + ": ");
                } else {
                    textViewKey.setText("");
                }
                textViewKey.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                rowLayout.addView(textViewKey);

                // Генерация UI для значения в зависимости от типа
                if (value instanceof JSONObject) {
                    JSONObject property = (JSONObject) value;
                    String type = property.getString("type");
                    switch (type) {
                        case "string":
                        case "number":
                            addEditableField(rowLayout, key, property);
                            break;
                        case "boolean":
                            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                            addCheckBox(rowLayout, key, property);
                            break;
                    }
                } else if (value instanceof JSONArray) {
                    if (key.equals("Select store")) {
                        storeArray = (JSONArray) value;
                        addSpinner(rowLayout, key, storeArray);
                    }
                }

                // Добавляем строку в контейнер
                layoutContainer.addView(rowLayout);
            }
        } catch (JSONException e) {
            Log.e("JSONError", "Error generating UI from JSON", e);
        }
    }

    private String getTextForAttribute(Object value) {
        try {
            if (value instanceof JSONObject) {
                JSONObject property = (JSONObject) value;
                if (property.has("text")) {
                    return property.getString("text");
                }
            }
        } catch (JSONException e) {
            Log.e("JSONError", "Error getting text for attribute", e);
        }
        return "";
    }

    private void addEditableField(LinearLayout rowLayout, String key, JSONObject property) {
        try {
            String value = property.getString("value");
            TextView textViewValue = new TextView(this);
            textViewValue.setText(value);
            textViewValue.setTag(key); // Сохраняем ключ в теге
            textViewValue.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textViewValue.setOnClickListener(v -> openEditDialog(key, property));
            rowLayout.addView(textViewValue);
        } catch (JSONException e) {
            Log.e("JSONError", "Error adding editable field", e);
        }
    }

    private void addCheckBox(LinearLayout rowLayout, String key, JSONObject property) {
        try {
            boolean value = property.getBoolean("value");
            androidx.appcompat.widget.AppCompatCheckBox checkBox = new androidx.appcompat.widget.AppCompatCheckBox(this);
            checkBox.setTag(key); // Сохраняем ключ в теге
            checkBox.setChecked(value);
            checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    property.put("value", isChecked);
                } catch (JSONException e) {
                    Log.e("JSONError", "Error updating checkbox value", e);
                }
            });
            rowLayout.addView(checkBox);
        } catch (JSONException e) {
            Log.e("JSONError", "Error adding checkbox", e);
        }
    }

    private void addSpinner(LinearLayout rowLayout, String key, JSONArray storeArray) {
        storeSpinner = new Spinner(this);
        List<String> storeNames = new ArrayList<>();
        int selectedPosition = 0;

        try {
            for (int i = 0; i < storeArray.length(); i++) {
                JSONObject store = storeArray.getJSONObject(i);
                storeNames.add(store.getString("text")); // Используем поле "text"
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

        storeSpinner.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.addView(storeSpinner);
    }

    private void openEditDialog(String key, JSONObject property) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + getTextForAttribute(property));

        final EditText input = new EditText(this);
        try {
            input.setText(property.getString("value"));
        } catch (JSONException e) {
            Log.e("JSONError", "Error getting value for dialog", e);
        }
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    property.put("value", input.getText().toString());
                    generateUIFromJSON(); // Обновляем UI
                } catch (JSONException e) {
                    Log.e("JSONError", "Error updating value", e);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
            // Сохраняем JSON в файл
            File file = new File(getCacheDir(), fileNameSetup);
            JSONHelper.saveJSONToFile(jsonObject, file);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACTION","RELOAD_APP");
            setResult(RESULT_OK, resultIntent);
            finish();
        } catch (IOException e) {
            Log.e("SetupApp", "saveJSONToFile: Error saving JSON", e);
        }
    }

    public static JSONObject getConfigJSON(AppCompatActivity appCompatActivity) {
        JSONObject CONFIG = new JSONObject();
        try {
            File fileConfig = new File (appCompatActivity.getCacheDir(), fileNameSetup) ;
            if (!fileConfig.exists()) {
                Intent intent = new Intent(appCompatActivity, SetupApp.class);
                appCompatActivity.startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
                return null;
//                try {
//                    JSONHelper.saveJSONToFile(createDefaultJSON(), fileConfig);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                Intent intent = new Intent(appCompatActivity, SetupApp.class);
//                appCompatActivity.startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
//                return null;
            }
            JSONObject jsonObject = new JSONObject(readTextFile(fileConfig.getParentFile(),fileConfig.getName()));
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = jsonObject.get(key);
                if (value instanceof JSONArray) {
                    // Обработка JSONArray
                    JSONArray jsonArray = (JSONArray) value;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject nestedObject = jsonArray.getJSONObject(i);
                        if (nestedObject.getBoolean("isSeletedRow")) {
                            CONFIG.put(key, nestedObject.get("value"));
                            break;
                        }
                    }
                } else if (value instanceof JSONObject) {
                    // Обработка JSONObject
                    JSONObject nestedObject = (JSONObject) value;
                    CONFIG.put(key, nestedObject.get("value"));
                } else {
                    // Простые значения (строка, число, boolean)
                    CONFIG.put(key, value);
                }
            }
        } catch (JSONException e) {
            Log.e("SetupApp", "getConfigJSON: Error saving JSON", e);
        }
        if (CONFIG.toString().equals("{}")) {
            Intent intent = new Intent(appCompatActivity, SetupApp.class);
            appCompatActivity.startActivityForResult(intent, SetupApp.REQUEST_CODE_SETUP_APP);
            return null;
        }
        return CONFIG;
    }


    public  static void onActivityResult(AppCompatActivity appCompatActivity,int requestCode, int resultCode, @Nullable Intent data,CallbackEmptyReturn callbackEmptyReturn) {
        if (requestCode == SetupApp.REQUEST_CODE_SETUP_APP && resultCode == RESULT_OK && data != null) {
            String reloadAppStr = data.getStringExtra("RELOAD_APP");
            if (reloadAppStr != null) {
                if (callbackEmptyReturn!=null){
                    callbackEmptyReturn.call();
                }
            }
        }

    }

    public static String readTextFile(File directory, String fileName) {
        File file = new File(directory.getAbsolutePath()+"/"+fileName);
        StringBuilder content = new StringBuilder();
        if (!file.exists()) {
            Log.e("SetupApp", "readTextFile: File does not exist");
            return null;
        }
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("SetupApp", "readTextFile: " + e.toString());
        }
        return content.toString();
    }
}