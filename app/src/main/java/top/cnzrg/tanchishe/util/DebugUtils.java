package top.cnzrg.tanchishe.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.R;

public class DebugUtils {
    public static boolean debug = false;

    public static void addGridLine(Context context, ViewGroup view) {
        if (!debug)
            return;

        int bianchang = GameData.GRID_LINE_LEN;
        // 横线
        for (int i = 0; i < GameData.SCENE_HEIGHT / bianchang + 1; i++) {
            View v = new View(context);
            v.setBackgroundColor(context.getResources().getColor(R.color.black));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            layoutParams.topMargin = bianchang * i;
            layoutParams.topToTop = R.id.game_scene;
            v.setLayoutParams(layoutParams);

            view.addView(v);
        }

        // 竖线
        for (int i = 0; i < GameData.SCENE_WIDTH / bianchang + 1; i++) {
            View v = new View(context);
            v.setBackgroundColor(context.getResources().getColor(R.color.black));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = bianchang * i;
            layoutParams.startToStart = R.id.game_scene;
            v.setLayoutParams(layoutParams);

            view.addView(v);
        }
    }
}
