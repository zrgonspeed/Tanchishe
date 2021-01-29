package top.cnzrg.tanchishe;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.WindowUtils;

public abstract class BaseActivity extends Activity {
    String[] pers = {Manifest.permission.SYSTEM_ALERT_WINDOW};

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

        //判断当前系统是否高于或等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < pers.length; i++) {
                if (!hasPermission(pers[i])) {
                    // 没有这个权限，去申请
                    ActivityCompat.requestPermissions(this, pers, 1);
                }
            }
        } else {
            //当前系统小于6.0，直接调用
            init();
        }

        init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length >= 1) {

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        // 没有这个权限
                        Logger.e("没有权限:" + permissions[i]);
                    }
                }



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
