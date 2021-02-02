package top.cnzrg.tanchishe;

import android.app.Application;

import top.cnzrg.tanchishe.music.MusicManager;
import top.cnzrg.tanchishe.music.lrc.LrcUtils;
import top.cnzrg.tanchishe.util.CrashHandler;
import top.cnzrg.tanchishe.util.StorageParam;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        StorageParam.setContext(getApplicationContext());
        LrcUtils.setContext(getApplicationContext());
        MusicManager.setContext(getApplicationContext());

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
    }

}
