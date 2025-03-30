package ru.miacomsoft.huawei_meta.file;

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

                JpegMetaInfo.updateXmp(filePanoJson,objJson,null);
                JpegMetaInfo.updateXmp(infoFilePath,imageInfoJson,()->{
                    getPhoto(webViewId, filePano, new JSONObject());
                });

                File infoFilePath = new File(panoramaPathJson);
                imageInfoJson = JpegMetaInfo.readJsonFromXmp(infoFilePath);
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("pitch",new BigDecimal(Double.valueOf(loocAtJson.getString("pitch"))));
                imageInfoJson.getJSONObject("scenes").getJSONObject("scene1").put("yaw",new BigDecimal(Double.valueOf(loocAtJson.getString("yaw"))));
                JpegMetaInfo.updateXmp(infoFilePath,imageInfoJson,null);


 */
public class JpegMetaInfo {
    public interface CallbackEmptyReturn {
        void call();
    }

    // Метод для извлечения JSON данных из блока <dc:json>
    public static JSONObject readJsonFromXmp(File jpegFile) {
        String xmpData = readXmp(jpegFile).toString();
        String startTag = "<GPano:JsonInfo>";
        String stopTag = "</GPano:JsonInfo>";
        if (xmpData != null && xmpData.length()>0) {
            // Ищем начало блока <dc:json>
            int jsonStart = xmpData.indexOf(startTag);
            if (jsonStart != -1) {
                jsonStart += startTag.length();
                // Ищем конец блока </dc:json>
                int jsonEnd = xmpData.indexOf(stopTag, jsonStart);
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

    public static StringBuilder readXmp(File src) {
        String startTag = "<x:xmpm";
        String stopTag = "</x:xmpmeta>";
        StringBuilder currentData = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(src);
            byte[] buffer = new byte[1024];
            int length;
            boolean insideXmp = false;
            boolean xmpReplaced = false;
            while ((length = fis.read(buffer)) > 0) {
                if (!xmpReplaced) {
                    String frag = new String(buffer);
                    int startIndex = frag.indexOf(startTag);
                    int endIndex = frag.indexOf(stopTag);

                    if (startIndex != -1) {
                        insideXmp = true;
                        currentData.append(frag.substring(startIndex));
                    } else if (endIndex != -1) {
                        insideXmp = false;
                        xmpReplaced = true;
                        currentData.append(frag.substring(0,endIndex+stopTag.length()));
                        break;
                    } else if (insideXmp && !xmpReplaced) {
                        currentData.append(frag);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при обработке файла", e);
        }
        return currentData;
    }

    public static String readXmpold(File jpegFile){
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

    public static File getTempFile(File src) {
        File dest = new File(src.getParentFile().getParentFile(), src.getName().substring(0, src.getName().lastIndexOf(".")) + "_tmp.jpg");
        JpegMetaInfo.copyFile(src,dest);
        return dest;
    }

    public static void updateXmp(File imgFile, JSONObject jsonMeta,CallbackEmptyReturn callbackEmptyReturn) {
        File dest = new File(imgFile.getParentFile().getParentFile(), imgFile.getName().substring(0, imgFile.getName().lastIndexOf(".")) + "_tmp.jpg");
        JpegMetaInfo.copyFile(imgFile,dest);
        JpegMetaInfo.addOrUpdateXmp(dest,jsonMeta,imgFile);
        if (callbackEmptyReturn !=null){
            callbackEmptyReturn.call();
        }
        dest.delete();
    }


    public static void addOrUpdateXmp(File src, JSONObject jsonMeta, File dest) {
        addOrUpdateXmp( src,  jsonMeta,  dest,null);
    }

    public static void addOrUpdateXmp(File src, JSONObject jsonMeta, File dest,CallbackEmptyReturn callbackEmptyReturn) {
        StringBuilder xmpData = new StringBuilder();
        xmpData.append("<x:xmpmeta xmlns:x='adobe:ns:meta/' x:xmptk='CV60'>\n");
        xmpData.append("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n");
        xmpData.append("    <rdf:Description rdf:about='' xmlns:GPano='http://ns.google.com/photos/1.0/panorama/'>\n");
        xmpData.append("        <GPano:UsePanoramaViewer>True</GPano:UsePanoramaViewer>\n");
        xmpData.append("        <GPano:ProjectionType>equirectangular</GPano:ProjectionType>");
        xmpData.append("        <GPano:PosePitchDegrees>0.0</GPano:PosePitchDegrees>");
        xmpData.append("        <GPano:PoseRollDegrees>0.0</GPano:PoseRollDegrees>");
        xmpData.append("        <GPano:CroppedAreaLeftPixels>0</GPano:CroppedAreaLeftPixels>");
        xmpData.append("        <GPano:CroppedAreaTopPixels>0</GPano:CroppedAreaTopPixels>");
        xmpData.append("        <GPano:JsonInfo>").append(jsonMeta.toString()).append("</GPano:JsonInfo>\n");
        xmpData.append("    </rdf:Description>\n");
        xmpData.append("</rdf:RDF>\n");
        xmpData.append("</x:xmpmeta>");

        byte[] newXmpData = xmpData.toString().getBytes(); // Новый блок XMP в виде байтов
        try {
            // Создаем временный файл для записи результата
            if ((dest != null) && (src.getAbsolutePath().toLowerCase().equals(dest.getAbsolutePath().toLowerCase()))) {
                // если исходный файл является результирующим, тогда  обнуляем  имя результирующего файла
                dest = null;
            }
            if (dest.exists()) {
                dest.delete();
                Thread.sleep(500);
            }
            dest.createNewFile();
            Thread.sleep(500);
            try (FileInputStream fis = new FileInputStream(src);
                 FileOutputStream fos = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int length;
                boolean insideXmp = false;
                boolean xmpReplaced = false;
                StringBuilder currentData = new StringBuilder();
                while ((length = fis.read(buffer)) > 0) {
                    if (!xmpReplaced) {
                        currentData.append(new String(buffer, 0, length, "UTF-8"));
                        int startIndex = currentData.indexOf("<x:xmpmeta");
                        if (startIndex != -1 && !insideXmp) {
                            insideXmp = true;
                            fos.write(currentData.substring(0, startIndex).getBytes("UTF-8"));
                            currentData.delete(0, startIndex);
                        }
                        int endIndex = currentData.indexOf("</x:xmpmeta>");
                        if (endIndex != -1 && insideXmp) {
                            insideXmp = false;
                            xmpReplaced = true;
                            fos.write(newXmpData);
                            currentData.delete(0, endIndex + "</x:xmpmeta>".length());
                        }

                        // Если блок XMP не найден, записываем данные как есть
                        if (!insideXmp && !xmpReplaced) {
                            fos.write(buffer, 0, length);
                            currentData.setLength(0);
                        }
                    } else {
                        fos.write(buffer, 0, length);
                    }
                }
                // Записываем оставшиеся данные, если они есть
                if (currentData.length() > 0) {
                    fos.write(currentData.toString().getBytes("UTF-8"));
                }
                fos.flush();
            }
            if (callbackEmptyReturn!=null) {
                callbackEmptyReturn.call();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Ошибка при обработке файла", e);
        }
    }

    public static void copyFile(File src, File dest) {
        try {
            // Проверяем, существует ли исходный файл
            if (!src.exists()) {
                throw new IOException("Source file does not exist: " + src.getAbsolutePath());
            }
            if (dest.exists()) {
                dest.delete();
                Thread.sleep(500);
            }
            dest.createNewFile();
            Thread.sleep(500);
            FileInputStream fis = null;
            FileOutputStream fos =null;
            try {
                fis = new FileInputStream(src);
                fos = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
