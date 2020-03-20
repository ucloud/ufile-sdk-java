package cn.ucloud.ufile.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: joshua
 * @E-mail: joshua.yin@ucloud.cn
 * @date: 2018/11/9 14:20
 */
public class JLog {
    private static volatile JLog mInstance;

    private boolean isSaveLog = false;

    public static boolean SHOW_TEST = false;
    public static boolean SHOW_DEBUG = false;
    public static boolean SHOW_VERBOSE = false;
    public static boolean SHOW_INFO = true;
    public static boolean SHOW_WARN = true;
    public static boolean SHOW_ERROR = true;

    private static final String LOG_POSITION_FORMAT = "[(%s:%s)#%s]: ";

    private JLog() {

    }

    private static JLog getInstance() {
        if (mInstance == null) {
            synchronized (JLog.class) {
                if (mInstance == null) {
                    mInstance = new JLog();
                }
            }
        }

        return mInstance;
    }

    public static void init(String basePath, int maxSaveDays, boolean isSaveLog) {
        getInstance();
        mInstance.isSaveLog = isSaveLog;
        if (isSaveLog) {
            LogFile.initBasePath(basePath, maxSaveDays);
        }
    }

    private static String getLogPosition() {
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();
        if (trace == null || trace.length < 3)
            return "<unknown>";

        return String.format(LOG_POSITION_FORMAT, trace[2].getFileName(), trace[2].getLineNumber(),
                trace[2].getMethodName());
    }

    public static void V(String TAG, String info) {
        if (SHOW_VERBOSE)
            System.out.println("VERBOSE-> " + TAG + ": " + getLogPosition() + info);
    }

    public static void D(String TAG, String info) {
        if (SHOW_DEBUG)
            System.out.println("DEBUG-> " + TAG + ": " + getLogPosition() + info);
    }

    public static void T(String TAG, String info) {
        if (SHOW_TEST)
            System.out.println("TEST-> " + TAG + ": " + getLogPosition() + info);
    }

    public static void I(String TAG, String info) {
        if (SHOW_INFO)
            System.out.println("INFO-> " + TAG + ": " + info);
    }

    public static void W(String TAG, String info) {
        if (SHOW_WARN)
            System.out.println("WARN-> " + TAG + ": " + getLogPosition() + info);
    }

    public static void E(String TAG, String info) {
        if (SHOW_ERROR)
            System.out.println("ERROR-> " + TAG + ": " + getLogPosition() + info);
    }

    public static void V(String TAG, String info, Throwable throwable) {
        if (SHOW_VERBOSE)
            System.out.println("VERBOSE-> " + TAG + ": " + getLogPosition() + info + "\n" + throwable);
    }

    public static void D(String TAG, String info, Throwable throwable) {
        if (SHOW_DEBUG)
            System.out.println("DEBUG-> " + TAG + ": " + getLogPosition() + info + "\n" + throwable);
    }

    public static void T(String TAG, String info, Throwable throwable) {
        if (SHOW_TEST)
            System.out.println("TEST-> " + TAG + ": " + getLogPosition() + info + "\n" + throwable);
    }

    public static void I(String TAG, String info, Throwable throwable) {
        if (SHOW_INFO)
            System.out.println("INFO-> " + TAG + ": " + info + "\n" + throwable);
    }

    public static void W(String TAG, String info, Throwable throwable) {
        if (SHOW_WARN)
            System.out.println("WARN-> " + TAG + ": " + getLogPosition() + info + "\n" + throwable);
    }

    public static void E(String TAG, String info, Throwable throwable) {
        if (SHOW_ERROR)
            System.out.println("ERROR-> " + TAG + ": " + getLogPosition() + info + "\n" + throwable);
    }

    public static void saveLog(String TAG, String info) {
        if (mInstance != null && mInstance.isSaveLog) {
            info = getLogPosition() + " " + info;
            if (SHOW_DEBUG)
                System.out.println("SAVE-> " + TAG + ": " + info);
            LogFile.writeLog(TAG + ": " + info);
        }
    }

    public static void saveLog(String TAG, String info, Throwable e) {
        if (mInstance != null && mInstance.isSaveLog) {
            info = getLogPosition() + " " + info;
            if (e != null)
                info += "\n" + e;

            if (SHOW_ERROR)
                System.out.println("SAVE-> " + TAG + ": " + info + "\n" + e);
            LogFile.writeLog(TAG + ": " + info);
        }
    }

    public static class LogFile {
        private static final String TAG = "LogFile";
        private static final String LOG_FILE_SUFFIX = ".log";
        private static String sLogBasePath;

        /**
         * 读写文件的线程池，单线程模型
         */
        private static ExecutorService sExecutorService;

        static {
            sExecutorService = Executors.newSingleThreadExecutor();
        }

        /**
         * 设置Log存放位置，同时删除超过存放时长的Log
         *
         * @param basePath
         */
        public static void initBasePath(String basePath, int maxSaveDays) {
            sLogBasePath = basePath;
            if (!new File(basePath).exists()) {
                new File(basePath).mkdirs();
            }
            delOldFiles(new File(basePath), maxSaveDays);
        }

        /**
         * 删除文件夹下所有的 N 天前创建的文件
         * 注意: 由于拿不到文件的创建时间，这里暂且拿最后修改时间比较
         *
         * @param dir
         * @param days
         */
        public static void delOldFiles(File dir, int days) {
            int daysMillis = days * 24 * 60 * 60 * 1000;
            if (dir.exists()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].isFile() && System.currentTimeMillis() - files[i].lastModified() > daysMillis) {
                            files[i].delete();
                        }
                    }
                }
            }
        }

        /**
         * 把文本写入文件中
         *
         * @param file       目录文件
         * @param content    待写内容
         * @param isOverride 写入模式，true - 覆盖，false - 追加
         */
        public static void write(final File file, final String content, final boolean isOverride) {
            sExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    FileOutputStream fos = null;
                    try {
                        boolean isExist = file.exists();
                        fos = new FileOutputStream(file, !(!isExist || isOverride));
                        fos.write(content.getBytes("UTF-8"));
                    } catch (IOException e) {
                        System.out.println(TAG + ": " + e);
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                System.out.println(TAG + ": " + e);
                            }
                        }
                    }
                }
            });
        }

        public static void writeLog(String content) {
            content = content.replaceFirst("]", "] \n");
            write(getLogFile(), "\n[" + getFormattedSecond() + "]" + content + "\n\n", false);
        }

        /**
         * 拿到最新的Log文件
         *
         * @return
         */
        public static File getLogFile() {
            File dir = new File(sLogBasePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File logFile = new File(dir, getFormattedDay() + LOG_FILE_SUFFIX);
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return logFile;
        }

        //==================================== TimeUtil =============================================//
        public static final String FORMATTER_DAY = "yy_MM_dd";
        public static final String FORMATTER_SECOND = "yy-MM-dd HH:mm:ss";

        public static SimpleDateFormat sSecondFormat = new SimpleDateFormat(FORMATTER_SECOND, Locale.CHINA);

        public static String getFormattedDay() {
            return new SimpleDateFormat(FORMATTER_DAY, Locale.CHINA).format(new Date());
        }

        public static String getFormattedSecond() {
            return sSecondFormat.format(new Date());
        }
    }
}
