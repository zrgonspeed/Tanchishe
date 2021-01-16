package top.cnzrg.tanchishe.util;

import android.util.Log;

public class Logger {

    public static boolean isDebug = true;

    private static final String TAG = "tag";
    private Logger(){
        throw new UnsupportedOperationException("不可以被实例！");
    }

    public static void i(String msg){
        if(isDebug){
            Log.i(TAG,msg);
        }
    }

    public static void d(String msg){
        if(isDebug){
            Log.d(TAG,msg);
        }
    }

    public static void e(String msg){
        if(isDebug){
            Log.e(TAG,msg);
        }
    }

    public static void v(String msg){
        if(isDebug){
            Log.v(TAG,msg);
        }
    }

    /*************************  下面是传入自定义tag的函数  ******************************/
    public static void i(String tag, String msg){
        if(isDebug){
            Log.i(tag,msg);
        }
    }

    public static void d(String tag, String msg){
        if(isDebug){
            Log.d(tag,msg);
        }
    }

    public static void e(String tag, String msg){
        if(isDebug){
            Log.e(tag,msg);
        }
    }

    public static void v(String tag, String msg){
        if(isDebug){
            Log.v(tag,msg);
        }
    }

    public static void w(String tag, String msg) {
        if(isDebug){
            Log.w(tag,msg);
        }
    }
}