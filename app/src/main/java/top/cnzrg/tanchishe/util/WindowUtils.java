package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import top.cnzrg.tanchishe.R;
import top.cnzrg.tanchishe.param.GameData;

public class WindowUtils {
    //隐藏虚拟按键，并且全屏
    public static void hideBottomUIMenu(Window window) {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = window.getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void fillRightAndBottom(Context context, ViewGroup group, int width, int height) {
        // 填充屏幕右边和下边
        View view = new View(context);
        view.setBackgroundColor(R.color.black);
        view.setX(GameData.SCENE_WIDTH);
        view.setY(0);

        view.setLayoutParams(new RelativeLayout.LayoutParams(width - GameData.SCENE_WIDTH, height));
        group.addView(view);

        View view2 = new View(context);
        view2.setBackgroundColor(R.color.black);
        view2.setX(0);
        view2.setY(GameData.SCENE_HEIGHT);

        view2.setLayoutParams(new RelativeLayout.LayoutParams(width, height - GameData.SCENE_HEIGHT));
        group.addView(view2);
    }
}
