package top.cnzrg.tanchishe.goal;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.security.SecureRandom;

import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.util.Logger;

public class GoalViewCreator {
    private static SecureRandom random = new SecureRandom();

    public static View createView(Context context, int[] goalDrawable) {
        int i = random.nextInt(goalDrawable.length);
        Logger.e("生成图片随机数:" + i);
        return createView(context, goalDrawable[i]);
    }

    public static View createView(Context context, int resId) {
        ImageView goalView = new ImageView(context);
        goalView.setImageResource(resId);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));
        return goalView;
    }

}
