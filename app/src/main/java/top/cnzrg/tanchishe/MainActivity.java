package top.cnzrg.tanchishe;


import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

public class MainActivity extends Activity implements IControlSnackView, IControlGoalView {
    private ControlSnack controlSnack;
    private ControlGoal controlGoal;

    private ImageView snack_head;

    private FloatingActionButton dire_up;
    private FloatingActionButton dire_right;
    private FloatingActionButton dire_down;
    private FloatingActionButton dire_left;

    private Handler mRunHandler;
    private Handler mCollHandler;

    private CollGoal collGoal;
    private CollSnack collSnack;
    private RelativeLayout game_scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControlSnack();

        initControlGoal();

        initUI();

        initListener();

    }

    private boolean isFirst = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        System.out.println("onWindowFocusChanged " + hasFocus);

        if (isFirst) {
            return;
        }
        isFirst = true;

        // 屏幕宽高获取
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        int width = metric.widthPixels; // 宽度（PX）
        int height = metric.heightPixels; // 高度（PX）

        GameData.SCENE_HEIGHT = height;
        GameData.SCENE_WIDTH = width;

        System.out.println("SCENE_HEIGHT " + height);
        System.out.println("SCENE_WIDTH " + width);

        gameStart();
    }

    private void initUI() {
        game_scene = findViewById(R.id.game_scene);

        snack_head = findViewById(R.id.snack_head);

        dire_up = findViewById(R.id.dire_up);
        dire_right = findViewById(R.id.dire_right);
        dire_down = findViewById(R.id.dire_down);
        dire_left = findViewById(R.id.dire_left);
    }

    private void initListener() {
        mRunHandler = new RunHandler();
        mCollHandler = new CollHandler();

        dire_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getControlSnack().turnUP();
            }
        });

        dire_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getControlSnack().turnRight();
            }
        });

        dire_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getControlSnack().turnDown();
            }
        });

        dire_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getControlSnack().turnLeft();
            }
        });
    }

    private void gameStart() {
        // 获取蛇ui

        // 按钮事件

        // 让蛇自动往一个方向移动

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    mRunHandler.sendEmptyMessage(controlSnack.getDirection());
                    try {
                        Thread.sleep(GameData.SNACK_MOVE_TIME_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // 随机出现一个目标
        createCollGoal();

        // 蛇的碰撞体设置
        createCollSnack();

        // 碰撞检测线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    mCollHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(GameData.COLL_GOAL_TIME_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void createCollSnack() {
        collSnack = new CollSnack();
        collSnack.setSnack(getControlSnack().getSnack());
        collSnack.setView(snack_head);
    }

    Random random = new Random();

    private void createCollGoal() {
        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(R.drawable.goal);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - goalView.getWidth() + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - goalView.getHeight() + 1));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        collGoal = new CollGoal();
        collGoal.setGoal(getControlGoal().getGoal());
        collGoal.setView(goalView);

        System.out.println("目标生成:" + collGoal.getName());
        System.out.println("goalView:" + goalView.getX() + " - " + goalView.getY());
    }

    private void initControlSnack() {
        controlSnack = getControlSnack();
        controlSnack.registerSnack(new Snack());
        controlSnack.setContext(this);
        controlSnack.setView(this);
    }

    private void initControlGoal() {
        controlGoal = getControlGoal();
        controlGoal.registerGoal(new Goal());
        controlGoal.setContext(this);
        controlGoal.setView(this);
    }

    @Override
    public ControlSnack getControlSnack() {
        return ControlSnack.getInstance();
    }

    @Override
    public ControlGoal getControlGoal() {
        return ControlGoal.getInstance();
    }

    @Override
    public void turnUP() {

    }

    @Override
    public void turnLeft() {

    }

    @Override
    public void turnRight() {
        System.out.println("往右");
    }

    @Override
    public void turnDown() {

    }

    class RunHandler extends Handler {
        @Override
        synchronized public void handleMessage(Message msg) {
            // 当前图片四条边处于边界上时，就不移动， game over

            if (msg.what == Direction.DIRECTION_UP) {
                if (collSnack.getView().getY() <= 0) {
                    return;
                }

                int newY = (int) (snack_head.getY() - GameData.SNACK_MOVE_DIST_INTERVAL);
                if (newY <= 0) {
                    System.out.println("game over DIRECTION_UP");
                    newY = 0;
                }
                collSnack.setY(newY);
            }

            if (msg.what == Direction.DIRECTION_RIGHT) {
                if (collSnack.getView().getX() >= GameData.SCENE_WIDTH - collSnack.getView().getWidth()) {
                    return;
                }
                int newX = (int) (snack_head.getX() + GameData.SNACK_MOVE_DIST_INTERVAL);
                if (newX >= GameData.SCENE_WIDTH - collSnack.getView().getWidth()) {
                    System.out.println("game over DIRECTION_RIGHT");
                    newX = GameData.SCENE_WIDTH - collSnack.getView().getWidth();
                }
                collSnack.setX(newX);
            }

            if (msg.what == Direction.DIRECTION_DOWN) {
                if (collSnack.getView().getY() >= GameData.SCENE_HEIGHT - collSnack.getView().getHeight()) {
                    return;
                }

                int newY = (int) (snack_head.getY() + GameData.SNACK_MOVE_DIST_INTERVAL);
                if (newY >= GameData.SCENE_HEIGHT - collSnack.getView().getHeight()) {
                    System.out.println("game over DIRECTION_DOWN");
                    newY = GameData.SCENE_HEIGHT - collSnack.getView().getHeight();
                }
                collSnack.setY(newY);
            }

            if (msg.what == Direction.DIRECTION_LEFT) {
                if (collSnack.getView().getX() <= 0) {
                    return;
                }

                int newX = (int) (snack_head.getX() - GameData.SNACK_MOVE_DIST_INTERVAL);
                if (newX <= 0) {
                    System.out.println("game over DIRECTION_LEFT");
                    newX = 0;
                }
                collSnack.setX(newX);
            }

        }
    }

    private class CollHandler extends Handler {
        boolean flag = false;

        @Override
        synchronized public void handleMessage(Message msg) {
            // if (相撞) -> 目标消失，重新随机出现


            if (collSnack == null || collGoal == null) {
                return;
            }

            //            System.out.println("snack:" + collSnack.getRect());
//            System.out.println("goal:" + collGoal.getRect());

            if (flag == true)
                return;

            if (collSnack.isColl(collGoal)) {
                flag = true;
                System.out.println("相撞");
                game_scene.removeView(collGoal.getView());

                collGoal = null;
                getControlGoal().unRegisterGoal();
                Goal goal = new Goal();
                goal.setName("目标-" + ++GameData.GOAL_COUNT);
                getControlGoal().registerGoal(goal);

                createCollGoal();

                flag = false;
            }


        }
    }
}