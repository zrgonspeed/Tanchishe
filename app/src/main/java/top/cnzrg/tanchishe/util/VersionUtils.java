package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionUtils {
    /**
     * @param
     * @explain 获取App版本号
     */
    public static String getAppVersionName(Context context) {
        synchronized (VersionUtils.class) {
            String versionName = "";
            try {
                // ---get the package info---
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
                versionName = pi.versionName;
                if (versionName == null || versionName.length() <= 0) {
                    return "";
                }
            } catch (Exception e) {
                Logger.e("VersionInfo", "Exception: " + e);
            }
            return versionName;
        }

    }
}
