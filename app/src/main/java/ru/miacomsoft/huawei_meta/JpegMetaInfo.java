package ru.miacomsoft.huawei_meta;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*

        File inputFile = new File("pic_20240605_184158.jpg");

        JSONObject jsonMetaInfo = new JSONObject();
        jsonMetaInfo.put("lat",10);
        jsonMetaInfo.put("lon",20);

        // Добавление или обновление XMP данных
        addOrUpdateXmp(inputFile,jsonMetaInfo,()->{
            System.out.println("XMP данные успешно добавлены или обновлены.");

            // Чтение XMP данных
            JSONObject readXmpData = readJsonFromXmp(inputFile);
            if (readXmpData != null) {
                System.out.println("Прочитанные XMP данные:\n" + readXmpData.toString(4));
            } else {
                System.out.println("XMP данные не найдены.");
            }
        });


 */
public class JpegMetaInfo {
    public interface CallbackEmptyReturn {
        void call();
    }

    public static void addOrUpdateXmp(File jpegFile,File jpegFileOut, JSONObject jsonMeta,CallbackEmptyReturn callbackEmptyReturn) {
        addOrUpdateXmp( jpegFile,jpegFileOut,  "",  "",  jsonMeta,callbackEmptyReturn);
    }
    public static void addOrUpdateXmp(File jpegFile, File jpegFileOut,JSONObject jsonMeta) {
        addOrUpdateXmp( jpegFile,jpegFileOut,  "",  "",  jsonMeta,null);
    }
    public static void addOrUpdateXmp(File jpegFile, File jpegFileOut,String title, JSONObject jsonMeta){
        addOrUpdateXmp( jpegFile,jpegFileOut,  title,  "",  jsonMeta,null);
    }
    public static void addOrUpdateXmp(File jpegFile,File jpegFileOut, String title, String description, JSONObject jsonMeta){
        addOrUpdateXmp( jpegFile,  jpegFileOut, title,  description,  jsonMeta,null);
    }

    public static void addOrUpdateXmp(File jpegFile, JSONObject jsonMeta,CallbackEmptyReturn callbackEmptyReturn) {
        addOrUpdateXmp( jpegFile,null,  "",  "",  jsonMeta,callbackEmptyReturn);
    }
    public static void addOrUpdateXmp(File jpegFile, JSONObject jsonMeta) {
        addOrUpdateXmp( jpegFile,null,  "",  "",  jsonMeta,null);
    }
    public static void addOrUpdateXmp(File jpegFile, String title, JSONObject jsonMeta){
        addOrUpdateXmp( jpegFile,null,  title,  "",  jsonMeta,null);
    }
    public static void addOrUpdateXmp(File jpegFile, String title, String description, JSONObject jsonMeta){
        addOrUpdateXmp( jpegFile,  null, title,  description,  jsonMeta,null);
    }

    public static void addOrUpdateXmp(File sourceFile, File destinationFile, String title, String description, JSONObject jsonMeta, CallbackEmptyReturn callbackEmptyReturn) {
        StringBuilder xmpData = new StringBuilder();
        xmpData.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xmpData.append("  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
        xmpData.append("    <rdf:Description rdf:about=\"\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n");
        xmpData.append("      <dc:title>").append(title).append("</dc:title>\n");
        xmpData.append("      <dc:description>").append(description).append("</dc:description>\n");
        xmpData.append("      <dc:json_info>").append(jsonMeta.toString()).append("</dc:json_info>\n");
        xmpData.append("    </rdf:Description>\n");
        xmpData.append("  </rdf:RDF>\n");
        xmpData.append("</x:xmpmeta>");
        boolean isCopyFile = true;
        if (destinationFile == null) {
            isCopyFile = false;
            destinationFile = new File(sourceFile.getParentFile(), sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")) + "_tmp.jpg");
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            destinationFile.createNewFile();
            Thread.sleep(1000);
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(destinationFile);
            byte[] xmpBytes = xmpData.toString().getBytes(StandardCharsets.UTF_8);
            byte[] xmpHeader = "http://ns.adobe.com/xap/1.0/\0".getBytes(StandardCharsets.UTF_8);
            int xmpLength = xmpBytes.length + xmpHeader.length + 2; // +2 для длины маркера
            boolean xmpFound = false;
            byte[] buffer = new byte[1024 * 1024]; // new byte[32192]; // Буфер для чтения данных
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                int offset = 0;
                while (offset < length) {
                    if (!xmpFound && buffer[offset] == (byte) 0xFF && offset + 1 < length && buffer[offset + 1] == (byte) 0xE1) {
                        // Найден маркер APP1
                        int app1Length = ((buffer[offset + 2] & 0xFF) << 8 | (buffer[offset + 3] & 0xFF));
                        if (offset + 4 + app1Length <= length) {
                            byte[] app1Data = new byte[app1Length - 2];
                            System.arraycopy(buffer, offset + 4, app1Data, 0, app1Length - 2);
                            // Проверяем, содержит ли APP1 блок XMP
                            if (startsWith(app1Data, xmpHeader)) {
                                xmpFound = true;
                                // Заменяем существующие XMP данные
                                outputStream.write(buffer, 0, offset);
                                outputStream.write(0xFF);
                                outputStream.write(0xE1);
                                outputStream.write((xmpLength >> 8) & 0xFF);
                                outputStream.write(xmpLength & 0xFF);
                                outputStream.write(xmpHeader);
                                outputStream.write(xmpBytes);
                                offset += 4 + app1Length;
                            } else {
                                // Копируем существующий APP1 блок
                                outputStream.write(buffer, offset, 4 + app1Length);
                                offset += 4 + app1Length;
                            }
                        } else {
                            // Если данные APP1 выходят за пределы буфера, обрабатываем их отдельно
                            outputStream.write(buffer, offset, length - offset);
                            offset = length;
                        }
                    } else {
                        outputStream.write(buffer, offset, 1);
                        offset++;
                    }
                }
            }

            // Если XMP блок не найден, добавляем его
            if (!xmpFound) {
                outputStream.write(0xFF);
                outputStream.write(0xE1);
                outputStream.write((xmpLength >> 8) & 0xFF);
                outputStream.write(xmpLength & 0xFF);
                outputStream.write(xmpHeader);
                outputStream.write(xmpBytes);
            }
            // Закрываем потоки
            outputStream.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (callbackEmptyReturn != null) {
                callbackEmptyReturn.call();
            }
        }
    }

    // Метод для извлечения JSON данных из блока <dc:json>
    public static JSONObject readJsonFromXmp(File jpegFile) {
        String xmpData = readXmp(jpegFile);
        if (xmpData != null) {
            // Ищем начало блока <dc:json>
            int jsonStart = xmpData.indexOf("<dc:json_info>");
            if (jsonStart != -1) {
                jsonStart += "<dc:json_info>".length();
                // Ищем конец блока </dc:json>
                int jsonEnd = xmpData.indexOf("</dc:json_info>", jsonStart);
                if (jsonEnd != -1) {
                    // Извлекаем JSON строку
                    String jsonString = xmpData.substring(jsonStart, jsonEnd);
                    try {
                        return new JSONObject(jsonString);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

    public static String readXmp(File jpegFile){
        try (FileInputStream fis = new FileInputStream(jpegFile)) {
            byte[] xmpHeader = "http://ns.adobe.com/xap/1.0/\0".getBytes(StandardCharsets.UTF_8);
            int marker;
            while ((marker = fis.read()) != -1) {
                if (marker == 0xFF) {
                    int nextByte = fis.read();
                    if (nextByte == 0xE1) { // APP1 маркер
                        int length = (fis.read() << 8) | fis.read();
                        byte[] app1Data = new byte[length - 2]; // Вычитаем 2 байта длины
                        fis.read(app1Data);
                        // Проверяем, содержит ли APP1 блок XMP
                        if (startsWith(app1Data, xmpHeader)) {
                            // Извлекаем XMP данные
                            int xmpDataLength = app1Data.length - xmpHeader.length;
                            byte[] xmpBytes = new byte[xmpDataLength];
                            System.arraycopy(app1Data, xmpHeader.length, xmpBytes, 0, xmpDataLength);
                            return new String(xmpBytes, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null; // XMP данные не найдены
    }
    //    private static boolean startsWith(byte[] array, byte[] prefix) {
//        if (array.length < prefix.length) {
//            return false;
//        }
//        for (int i = 0; i < prefix.length; i++) {
//            if (array[i] != prefix[i]) {
//                return false;
//            }
//        }
//        return true;
//    }
    // Вспомогательная функция для проверки начала массива байтов
    private static boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
