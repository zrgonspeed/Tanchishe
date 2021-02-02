package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @Description 这里用一句话描述
 * @Author GaleLiu
 * @Time 2019/07/22
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Context mContext;

    public CrashHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.e("程序出现异常了", "Thread = " + t.getName() + "\nThrowable = " + e.getMessage());
        String stackTraceInfo = getStackTraceInfo(e);
        Logger.e("stackTraceInfo", stackTraceInfo);
        saveThrowableMessage(stackTraceInfo);
    }

    /**
     * 获取错误的信息
     *
     * @param throwable
     * @return
     */
    private String getStackTraceInfo(final Throwable throwable) {
        PrintWriter pw = null;
        Writer writer = new StringWriter();
        try {
            pw = new PrintWriter(writer);
            throwable.printStackTrace(pw);
        } catch (Exception e) {
            return "";
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
        return writer.toString();
    }

    private String logFilePath = "/storage/udiskh/carsh.txt";

    private void saveThrowableMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return;
        }
        File file = new File(logFilePath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                writeStringToFile(errorMessage, file);
            }
        } else {
            writeStringToFile(errorMessage, file);
        }
    }

    private void writeStringToFile(final String errorMessage, final File file) {
        new Thread(() -> {
            FileOutputStream outputStream = null;
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(errorMessage.getBytes());
                outputStream = new FileOutputStream(new File(file, System.currentTimeMillis() + ".txt"));
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                }
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
