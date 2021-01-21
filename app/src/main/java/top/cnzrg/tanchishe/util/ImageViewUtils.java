package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.content.res.TypedArray;

public class ImageViewUtils {
    public static int[] getGoalDrawable(Context context, int arrayId) {
        TypedArray ar = context.getResources().obtainTypedArray(arrayId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        return resIds;
    }

}
