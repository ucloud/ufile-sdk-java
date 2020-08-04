package cn.ucloud.ufile.util;

import cn.ucloud.ufile.exception.UfileFileException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;

/**
 * @description:
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018-12-29 18:38
 */
public class MimeTypeUtil {
    private static String TAG = "MimeTypeUtil";
    private static String MIME_TYPE_JSONSTR;

    private static JsonObject MIME_TYPE_JSON;

    static {
        try {
            MIME_TYPE_JSONSTR = FileUtil.readFileContent(ClassLoader.getSystemResourceAsStream("mime-type.json"));
            MIME_TYPE_JSON = new Gson().fromJson(MIME_TYPE_JSONSTR, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getMimeType(File file) throws UfileFileException {
        if (file == null)
            throw new UfileFileException("File is null");

        if (!file.exists())
            throw new UfileFileException("File is not exist");

        if (!file.isFile())
            throw new UfileFileException(String.format("%s is not a file", file.getName()));

        String name = file.getName();
        return getMimeType(name);
    }

    public static String getMimeType(String fileName) {
        JLog.T(TAG, "[file name]:" + fileName);
        if (fileName == null || fileName == "" || !fileName.contains("."))
            return MIME_TYPE_JSON.get("").getAsString();

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > (fileName.length() - 2))
            fileName = "";
        else
            fileName = fileName.substring(lastDot + 1);

        JLog.T(TAG, "[suffix]:" + fileName);

        JsonElement res = MIME_TYPE_JSON.get(fileName);

        return res == null ? MIME_TYPE_JSON.get("").getAsString() : res.getAsString();
    }
}
