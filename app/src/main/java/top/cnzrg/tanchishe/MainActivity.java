package top.cnzrg.tanchishe;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Random;

import top.cnzrg.tanchishe.util.ToastUtil;

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
    private CollSnack collSnackHead;
    private CollSnack lastBody;
    private ConstraintLayout game_scene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        initControlSnack();

        initControlGoal();

        initUI();

        initListener();

    }

    private boolean isFirst = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        System.out.println("onWindowFocusChanged " + hasFocus);

        if (!isFirst) {
            return;
        }
        isFirst = false;

        // 屏幕宽高获取
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        int width = metric.widthPixels; // 宽度（PX）
        int height = metric.heightPixels; // 高度（PX）

        GameData.SCENE_HEIGHT = height;
        GameData.SCENE_WIDTH = width;

        System.out.println("SCENE_HEIGHT " + height);
        System.out.println("SCENE_WIDTH " + width);
        ToastUtil.showLong(this, "H " + height + " W " + width + " S " + snack_head.getWidth());

        gameStart();

        // 网格线添加
        addGridLine();
    }

    public void addGridLine() {
        int bianchang = GameData.GRID_LINE_LEN;
        // 横线
        for (int i = 0; i < GameData.SCENE_HEIGHT / bianchang + 1; i++) {
            View v = new View(this);
            v.setBackgroundColor(getResources().getColor(R.color.black));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            layoutParams.topMargin = bianchang * i;
            layoutParams.topToTop = R.id.game_scene;
            v.setLayoutParams(layoutParams);

            game_scene.addView(v);
        }

        // 竖线
        for (int i = 0; i < GameData.SCENE_WIDTH / bianchang + 1; i++) {
            View v = new View(this);
            v.setBackgroundColor(getResources().getColor(R.color.black));
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.leftMargin = bianchang * i;
            layoutParams.startToStart = R.id.game_scene;
            v.setLayoutParams(layoutParams);

            game_scene.addView(v);
        }
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
        collSnackHead = new CollSnack();
        collSnackHead.setSnack(getControlSnack().getSnack());
        collSnackHead.setView(snack_head);
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
        System.out.println("DIRECTION_UP");

        int newY = (int) (snack_head.getY() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY <= 0) {
            System.out.println("game over DIRECTION_UP");
            newY = 0;
        }
//                collSnackHead.setY(newY);
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    @Override
    public void turnLeft() {
        System.out.println("DIRECTION_LEFT");

        int newX = (int) (snack_head.getX() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX <= 0) {
            System.out.println("game over DIRECTION_LEFT");
            newX = 0;
        }
//                collSnackHead.setX(newX);
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnRight() {
        System.out.println("DIRECTION_RIGHT");

        int newX = (int) (snack_head.getX() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
            System.out.println("game over DIRECTION_RIGHT");
            newX = GameData.SCENE_WIDTH - collSnackHead.getView().getWidth();
        }
//                collSnackHead.setX(newX);
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnDown() {
        System.out.println("DIRECTION_DOWN");

        int newY = (int) (snack_head.getY() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY >= GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
            System.out.println("game over DIRECTION_DOWN");
            newY = GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight();
        }
//                collSnackHead.setY(newY);
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    private int lastDire = 0;

    class RunHandler extends Handler {
        @Override
        synchronized public void handleMessage(Message msg) {
            // 当前图片四条边处于边界上时，就不移动， game over


            if (msg.what == Direction.DIRECTION_UP) {
                if (collSnackHead.getView().getY() <= 0) {
                    return;
                }

                if (lastDire == Direction.DIRECTION_DOWN) {
                    turnDown();
                    return;
                }

                turnUP();

            }

            if (msg.what == Direction.DIRECTION_RIGHT) {
                if (collSnackHead.getView().getX() >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
                    return;
                }

                if (lastDire == Direction.DIRECTION_LEFT) {
                    turnLeft();
                    return;
                }

                turnRight();
            }

            if (msg.what == Direction.DIRECTION_DOWN) {
                if (collSnackHead.getView().getY() >= GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
                    return;
                }

                if (lastDire == Direction.DIRECTION_UP) {
                    turnUP();
                    return;
                }

                turnDown();
            }

            if (msg.what == Direction.DIRECTION_LEFT) {
                if (collSnackHead.getView().getX() <= 0) {
                    return;
                }

                if (lastDire == Direction.DIRECTION_RIGHT) {
                    turnRight();
                    return;
                }

                turnLeft();
            }

            lastDire = msg.what;
        }
    }

    private class CollHandler extends Handler {
        boolean flag = false;
        int a = 0;

        @Override
        synchronized public void handleMessage(Message msg) {
            // if (相撞) -> 目标消失，重新随机出现


            if (collSnackHead == null || collGoal == null) {
                return;
            }

            //            System.out.println("snack:" + collSnack.getRect());
//            System.out.println("goal:" + collGoal.getRect());

            if (flag == true)
                return;

            if (collSnackHead.isColl(collGoal)) {
                flag = true;
                System.out.println("相撞");
                game_scene.removeView(collGoal.getView());

                collGoal = null;
                getControlGoal().unRegisterGoal();
                Goal goal = new Goal();
                goal.setName("目标-" + ++GameData.GOAL_COUNT);
                getControlGoal().registerGoal(goal);

                createCollGoal();

                // 蛇身增加一块
                CollSnack body = new CollSnack();
                // snack实体还是通用
                body.setSnack(getControlSnack().getSnack());

                int[] arr = {
                        R.drawable.body1,
                        R.drawable.body2,
                        R.drawable.body3,
                        R.drawable.body4,
                        R.drawable.body5,
                        R.drawable.body6,
                        R.drawable.body7,
                        R.drawable.body8,
                };


                // 身体图片
                ImageView bodyView = new ImageView(MainActivity.this);
                bodyView.setImageResource(arr[a++]);
                bodyView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                // 设置身体图片宽高
                bodyView.setLayoutParams(new RelativeLayout.LayoutParams(GameData.SNACK_BODY_WIDTH_HEIGHT, GameData.SNACK_BODY_WIDTH_HEIGHT));

                if (lastBody == null) {
                    bodyView.setX(collSnackHead.getLastX());
                    bodyView.setY(collSnackHead.getLastY());
                } else {
                    bodyView.setX(lastBody.getLastX());
                    bodyView.setY(lastBody.getLastY());
                }
                game_scene.addView(bodyView);

                body.setView(bodyView);

                if (lastBody == null) {
                    lastBody = body;
                }

                if (collSnackHead.nextBody() == null) {
                    collSnackHead.addBody(body);
                } else {
                    lastBody.addBody(body);
                    lastBody = body;
                }

                flag = false;
            }


        }
    }
}