package top.cnzrg.tanchishe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.Arrays;

import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ToastUtils;
import top.cnzrg.tanchishe.util.WindowUtils;

public abstract class BaseActivity extends Activity {
    private String[] pers = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
    };

    private int requestCode = 1010;
    private int requestCode_alert = 1020;

    /**
     * 代替Settings.canDrawOverlays，之前授权悬浮窗返回后返回false。，
     * @param context
     * @return
     */
    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return Settings.canDrawOverlays(context);
        } else {
            if (Settings.canDrawOverlays(context)) return true;
            try {
                WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (mgr == null) return false; //getSystemService might return null
                View viewToAdd = new View(context);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
                viewToAdd.setLayoutParams(params);
                mgr.addView(viewToAdd, params);
                mgr.removeView(viewToAdd);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //隐藏虚拟按键，并且全屏
        WindowUtils.hideBottomUIMenu(getWindow());

        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        boolean ok = true;
        //判断当前系统是否高于或等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < pers.length; i++) {
                if (!hasPermission(pers[i])) {
                    Logger.e("没有这个权限:" + pers[i]);
                    ok = false;
                } else {
                    Logger.e("有这个权限");
                }
            }

            // 悬浮窗权限，跳转设置界面
            if (!canDrawOverlays(this)) {
                //启动Activity让用户授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, requestCode_alert);
            } else {
                Logger.e("有悬浮窗权限");
            }

            if (!ok) {
                Logger.e("请求权限pers[]");
                ActivityCompat.requestPermissions(this, pers, requestCode);
            } else {
                Logger.e("pers[]权限都有");
                init();
            }

        } else {
            //当前系统小于6.0，直接调用
            init();
        }

    }
    private boolean highVersionPermissionCheck(Context context) {
        try {
            Class clazz = Settings.class;
            Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
            return (Boolean) canDrawOverlays.invoke(null, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCode_alert) {
            if (canDrawOverlays(this)) {
                Logger.e("悬浮窗权限打开");
//                init();
            } else {
                Logger.e("没有授予悬浮窗权限");
                // 退出应用
                System.exit(1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode) {
            Logger.e("授权窗口点完后");

            Logger.e("grantResults: " + Arrays.toString(grantResults));
            Logger.e("permissions: " + Arrays.toString(permissions));
            if (grantResults.length >= 1) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // 没有这个权限
                        Logger.e("没有权限:" + permissions[i]);

                        // 提示用户无该权限就无法使用
                        ToastUtils.showLong(getApplicationContext(), "无权限使用");

                        // 退出应用
                        System.exit(1);
                    }
                }

                // 有权限
                init();
            }
        }
    }

    /**
     * 是否有这个权限
     *
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public abstract void init();
}
