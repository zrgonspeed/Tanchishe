package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences类，存放变量值
 * key value
 * String : String float long int boolean
 */
public class StorageParam {
    private static final String SP_NAME = "SP";
    private static Context context;

    public static void setContext(Context context) {
        StorageParam.context = context;
    }

    public static void setParam(String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(StorageParam.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getParam(String key, String dfValue) {
        SharedPreferences sp = context.getSharedPreferences(StorageParam.SP_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, dfValue);
    }
}
