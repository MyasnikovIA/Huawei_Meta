package ru.miacomsoft.huawei_meta.file;



import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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

import ru.miacomsoft.huawei_meta.photo360.Panorama;
import ru.miacomsoft.huawei_meta.setup.SetupApp;
import ru.miacomsoft.huawei_meta.utils.Dialog;


public class FileBrowser {
    public interface CallbackFileEmptyReturn {
        void call(File file);
    }
    public interface CallbackEmptyReturn {
        void call();
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

    private CallbackEmptyReturn onAfterDeleteEvent;

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
    public void getFileList(int ListViewFileId, int EditFileFilterId, String filePath,String selectPhoto) {
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
                boolean currentState = listView.isItemChecked(position);
                if (!currentState) {
                    listView.setItemChecked(position, currentState);
                } else {
                    listView.setItemChecked(position, !currentState);
                }
                if (onClickEvent!=null) {
                    onClickEvent.call(fileListAbs.get(fileList.get(position)));
                } else {
                    Toast.makeText(appCompatActivity, "Выбран файл: " + fileListAbs.get(fileList.get(position)).getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                boolean currentState = listView.isItemChecked(position);
                listView.setItemChecked(position, !currentState);
                return true;
            }
        });

        if (selectPhoto!=null) {
            selectRowByText(selectPhoto);
        }

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
    private void selectRowByText(String targetText) {
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).equals(targetText)) {
                // Устанавливаем выбор на найденной строке
                listView.setItemChecked(i, true);
                listView.smoothScrollToPosition(i); // Прокручиваем ListView к выбранной строке
                return;
            }
        }
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

    public void deleteFile(CallbackEmptyReturn onAfterDeleteEventLocal){
        this.onAfterDeleteEvent = onAfterDeleteEventLocal;
        Dialog.showConfirmDialog(appCompatActivity, "Подтверждение", "Вы уверены, что хотите файлы?", new Dialog.ConfirmDialogListener() {
            @Override
            public void onConfirm(boolean isConfirmed) {
                for (Object selectFile :  getSelectFile()) {
                    File oneFile = fileListAbs.get(selectFile);
                    if (oneFile.exists()) {
                        oneFile.delete();
                    }
                }

                if (onAfterDeleteEvent!=null) {
                    onAfterDeleteEvent.call();
                    onAfterDeleteEvent = null;
                }
            }
        });
    }

    private  List<Object> getSelectFile() {

        // Получение всех выбранных позиций
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        // Список для хранения выбранных элементов
        List<Object> selectedItems = new ArrayList<>();

        // Перебор всех элементов
        for (int i = 0; i < checked.size(); i++) {
            // Позиция элемента
            int position = checked.keyAt(i);

            // Проверяем, выбран ли элемент
            if (checked.valueAt(i)) {
                Object item = listView.getItemAtPosition(position);
                selectedItems.add(item);
            }
        }
        return selectedItems;
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
                            File fileInfoJson = new File(file.getParentFile(), fileName.substring(0,fileName.lastIndexOf("."))+".json");
                            if (!fileInfoJson.exists()) {
                                Panorama.createEmptyInfoFileJson(file);
                            }
                            String imageInfoJsonStr = SetupApp.readTextFile(fileInfoJson.getParentFile(), fileInfoJson.getName());
                            try {
                                JSONObject objJson = new JSONObject(imageInfoJsonStr);
                                JSONObject scenes1 = objJson.getJSONObject("scenes").getJSONObject("scene1");
                                String prefixStr = " (";
                                if (scenes1.getDouble("lat") == 0 && scenes1.getDouble("lon") == 0) {
                                    prefixStr += "NoGPS";
                                };
                                if (scenes1.getJSONArray("hotSpots").length()==0){
                                    //  prefixStr += "NoPoint ";
                                };
                                prefixStr += ")";
                                if (prefixStr.length()==3) {
                                    prefixStr = "";
                                }
                                fileList.add(file.getName());
                                fileListAbs.put(file.getName()+prefixStr,file);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                // Сортировка файлов по имени
                //  Collections.sort(fileList);
            }
            sortFileListByDate();
        } else {
            Toast.makeText(appCompatActivity, "Каталог не найден", Toast.LENGTH_SHORT).show();
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
