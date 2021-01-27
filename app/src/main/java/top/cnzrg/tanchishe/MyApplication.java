package top.cnzrg.tanchishe;

import android.app.Application;

import top.cnzrg.tanchishe.util.StorageParam;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        StorageParam.setContext(getApplicationContext());
    }
}
