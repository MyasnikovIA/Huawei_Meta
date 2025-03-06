package ru.miacomsoft.huawei_meta;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FileBrowser {
    public interface CallbackFileEmptyReturn {
        void call(File file);
    }

    /** ListView для отображения списка файлов */
    private ListView listView;

    /** EditText для фильтрации файлов */
    private EditText editFilter;

    /** Адаптер для ListView */
    private ArrayAdapter<String> adapter;

    /** Список всех файлов */
    private List<String> fileList;
    private HashMap<String,File> fileListAbs;

    private String PATH_DIR;

    private CallbackFileEmptyReturn onClickEvent;

    private AppCompatActivity appCompatActivity;
    public FileBrowser(AppCompatActivity appCompatActivity) {
        this.appCompatActivity  = appCompatActivity;
    }

    public void getFileList(int ListViewFileId, int EditFileFilterId, String filePath) {
        //listView = findViewById(R.id.FileListView);
        //editFilter = findViewById(R.id.editTextFilter);
        listView = appCompatActivity.findViewById(ListViewFileId);
        editFilter = appCompatActivity.findViewById(EditFileFilterId);

        PATH_DIR = filePath;
        fileList = new ArrayList<>();
        fileListAbs = new HashMap<>();
        loadFiles();

        // Инициализация адаптера
        adapter = new ArrayAdapter<>(appCompatActivity, android.R.layout.simple_list_item_multiple_choice, fileList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Добавление обработчика двойного клика
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (onClickEvent!=null) {
                    onClickEvent.call(fileListAbs.get(fileList.get(position)));
                }else {
                    String selectedFile = fileList.get(position);
                    Toast.makeText(appCompatActivity, "Выбран файл: " + selectedFile, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Добавление обработчика долгого клика для удаления файлов
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
                // Фильтрация списка при изменении текста
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
                            fileList.add(file.getName());
                            fileListAbs.put(file.getName(),file);
                        }
                    }
                }
                // Сортировка файлов по имени
                Collections.sort(fileList);
            }
        } else {
            Toast.makeText(appCompatActivity, "Каталог не найден", Toast.LENGTH_SHORT).show();
        }
    }


}
