package cn.ucloud.ufile.util;


import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/11 22:55
 */
public class FileUtil {
    private static final String TAG = "FileUtil";

    public static final int IO_BUFFER_SIZE = 256 << 10;

    public static File searchFile(File file, final String fileName, final boolean isDstIsFile) {
        if (file == null || !file.exists() || fileName == null || fileName.length() == 0)
            return null;

        JLog.T(TAG, "searchFile--->[isFile]:" + file.isFile() + " [isDir]:" + file.isDirectory());

        if (file.getName().equals(fileName)
                && ((isDstIsFile && file.isFile()) || (!isDstIsFile && file.isDirectory())))
            return file;

        ArrayList<File> subDirs = new ArrayList<>();
        File res = null;
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (File f : files) {
                JLog.T(TAG, "searchFile--->for-->[path]:" + f.getAbsolutePath() + " [name]:" + f.getName()
                        + " [isFile]:" + f.isFile() + " [isDir]:" + f.isDirectory());
                if (f.getName().equals(fileName)
                        && ((isDstIsFile && f.isFile()) || (!isDstIsFile && f.isDirectory()))) {
                    return f;
                } else {
                    if (f.isDirectory())
                        subDirs.add(f);
                }
            }

            for (File sd : subDirs) {
                if ((res = searchFile(sd, fileName, isDstIsFile)) != null)
                    return res;
            }
        }

        return res;
    }

    public static byte[] readSmallFileByteArr(File file) throws IOException {
        if (file == null || !file.exists())
            return null;

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        byte[] res = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[IO_BUFFER_SIZE];
            int len = -1;
            while ((len = bis.read(buffer)) > 0) {
                if (res == null) {
                    res = new byte[len];
                    System.arraycopy(buffer, 0, res, 0, len);
                } else {
                    byte[] tmp = new byte[res.length + len];
                    System.arraycopy(res, 0, tmp, 0, res.length);
                    System.arraycopy(buffer, 0, tmp, res.length, len);
                    res = tmp;
                }
            }
        } finally {
            close(bis, fis);
        }

        return res;
    }

    public static String readFileContent(InputStream is) throws IOException {
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer res = new StringBuffer();
        try {
            isr = new InputStreamReader(is, Charset.defaultCharset());
            br = new BufferedReader(isr);

            String buffer = null;
            while ((buffer = br.readLine()) != null) {
                res.append(buffer);
            }
        } finally {
            close(br, isr, is);
        }

        return res.toString();
    }

    public static String readSmallFileStringContent(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile())
            return null;

        FileReader fr = null;
        BufferedReader br = null;
        StringBuffer res = new StringBuffer();
        try {
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String buffer = null;
            int line = 0;
            while ((buffer = br.readLine()) != null) {
                res.append(buffer.trim());
            }
        } finally {
            close(br, fr);
        }

        return res.toString();
    }

    public static boolean checkFileMD5(File file, String md5) throws IOException {
        if (file == null || !file.exists() || !file.isFile())
            return false;

        if (md5 == null || md5.length() == 0)
            return false;

        String fileMD5 = null;
        try {
            fileMD5 = HexFormatter.formatByteArray2HexString(Encoder.md5(file), true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }

        JLog.T(TAG, "checkFileMD5--->[file]:" + fileMD5 + " [md5]:" + md5);

        return (fileMD5 != null && fileMD5.length() > 0 && fileMD5.equals(md5));
    }

    public static void deleteFileCleanly(File file) {
        if (file == null || !file.exists())
            return;

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files)
                deleteFileCleanly(f);
            file.delete();
        } else {
            boolean res = file.delete();
            JLog.T(TAG, "deleteFileCleanly--->" + file.getAbsolutePath() + " delete=" + res);
        }
    }

    public static void close(AutoCloseable... closeable) {
        if (closeable == null)
            return;

        for (AutoCloseable c : closeable) {
            if (c == null)
                continue;

            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
