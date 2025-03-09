package ru.miacomsoft.huawei_meta;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBrowser {
    public interface CallbackFileEmptyReturn {
        void call(File file);
    }

    private String TAG = "FileBrowser";

    /** ListView для отображения списка файлов */
    private ListView listView;

    /** EditText для фильтрации файлов */
    private EditText editFilter;

    /** Адаптер для ListView */
    private ArrayAdapter<String> adapter;

    /** Список всех файлов */
    private List<String> fileList;

    /** Переменная для хронения полного пути к файлу, по имени */
    private HashMap<String,File> fileListAbs;

    /** Путь к каталогу для сканирования */
    private String PATH_DIR;

    /** Событие нажатие на выбранный файл в listView */
    private CallbackFileEmptyReturn onClickEvent;

    /** ссылка на объект приложения */
    private AppCompatActivity appCompatActivity;

    public FileBrowser(AppCompatActivity appCompatActivity) {
        this.appCompatActivity  = appCompatActivity;
    }

    /**
     * Получить список файлов для визуализации
     * @param ListViewFileId
     * @param EditFileFilterId
     * @param filePath
     */
    public void getFileList(int ListViewFileId, int EditFileFilterId, String filePath) {
        listView = appCompatActivity.findViewById(ListViewFileId);
        editFilter = appCompatActivity.findViewById(EditFileFilterId);
        PATH_DIR = filePath;
        fileList = new ArrayList<>();
        fileListAbs = new HashMap<>();
        loadFiles();
        adapter = new ArrayAdapter<>(appCompatActivity, android.R.layout.simple_list_item_multiple_choice, fileList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        // Добавление обработчика двойного клика
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onClickEvent!=null) {
                    onClickEvent.call(fileListAbs.get(fileList.get(position)));
                } else {
                    Toast.makeText(appCompatActivity, "Выбран файл: " + fileListAbs.get(fileList.get(position)).getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Добавление обработчика долгого клика для удаления файлов
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // todo: дописать механизм удаление выбранного файла
                // deleteSelectedFiles();
                return true;
            }
        });

        // Добавление фильтрации по тексту
        editFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // todo: кандидат на удаление
                // adapter.getFilter().filter(s.toString()); // Фильтрация списка при изменении текста (старый вариант )
                filterFileList(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFileList(String filterText) {
        fileList.clear();
        for (Map.Entry<String, File> entry : fileListAbs.entrySet()) {
            fileList.add(entry.getKey());
        }
        List<String> filteredList = new ArrayList<>();
        for (String fileName : fileList) {
            if (fileName.toLowerCase().contains(filterText.toLowerCase())) {
                filteredList.add(fileName);
            }
        }
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }


    public void onClick(CallbackFileEmptyReturn onClickEvent){
        this.onClickEvent = onClickEvent;
    }

    /**
     * Загружает файлы из указанного каталога и сортирует их.
     */
    private void loadFiles() {
        File directory = new File(PATH_DIR);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase().equals("jpg")) {
                            File fileInfoJson = new File(fileName.substring(0,fileName.lastIndexOf("."))+".json");
                            if (!fileInfoJson.exists() || !fileInfoJson.isFile()) {
                                createEmptyInfoFileJson(file);
                            }
                            fileList.add(file.getName());
                            fileListAbs.put(file.getName(),file);
                        }
                    }
                }
                // Сортировка файлов по имени
                Collections.sort(fileList);
            }
            sortFileListByDate();
        } else {
            Toast.makeText(appCompatActivity, "Каталог не найден", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Процедура создания пустого информационного файла JSON
     * @param fileImg - файл изображения, для которого необходимо создать текстовой файл описания в формате JSON
     */
    private void createEmptyInfoFileJson(File fileImg) {
        String fileName = fileImg.getAbsolutePath();
        try {
            String name = fileName.substring(fileName.lastIndexOf("/")+1, fileName.lastIndexOf("."));
            JSONObject scen = new JSONObject();
            scen.put("default", new JSONObject("{\"firstScene\": \"scene1\"}"));
            scen.put("hotSpotDebug", false);
            scen.put("hotPointDebug", true);
            scen.put("sceneFadeDuration", 1000);
            JSONObject scene1 = new JSONObject();
            scene1.put("hotSpots", new JSONArray());
            scene1.put("panorama", fileName);
            scene1.put("autoLoad", true);
            scene1.put("crossOrigin", "use-credentials");
            scene1.put("lon", 0);
            scene1.put("lat", 0);
            scene1.put("orient_azimuth", 0);
            scene1.put("orient_roll", 0);
            scene1.put("orient_pitch", 0);
            scene1.put("title", "title:" + name);
            scene1.put("pitch", -3.5001450183561142);
            scene1.put("yaw", 172.69391364740358);
            JSONObject scene = new JSONObject();
            scene.put("scene1", scene1);
            scen.put("scenes", scene);
            createTextFile(fileImg.getParentFile(),name + ".json", scen.toString(4));
        } catch (JSONException e) {
            Log.e(TAG, "createEmptyInfoFileJson: " + e.toString());
        }
    }

    /**
     * Функция создания тек4стового файла
     * @param directory - каталог создания
     * @param fileName - имя создаваемого файла
     * @param content - содержимое, которе помещается в файл
     */
    private void createTextFile( File directory , String fileName, String content) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "createTextFile: " + e.toString());
        }
    }

    /**
     * Сортировка списка фаылов по дате создания файлов.
     */
    private void sortFileListByDate() {
        List<Map.Entry<String, File>> entries = new ArrayList<>(fileListAbs.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, File>>() {
            @Override
            public int compare(Map.Entry<String, File> entry1, Map.Entry<String, File> entry2) {
                long file1Date = entry1.getValue().lastModified();
                long file2Date = entry2.getValue().lastModified();
                return Long.compare(file1Date, file2Date);
            }
        });
        fileListAbs.clear();
        for (Map.Entry<String, File> entry : entries) {
            fileListAbs.put(entry.getKey(), entry.getValue());
        }
        fileList.clear();
        for (Map.Entry<String, File> entry : entries) {
            fileList.add(entry.getKey());
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
