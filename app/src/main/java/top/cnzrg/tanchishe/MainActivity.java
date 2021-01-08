package top.cnzrg.tanchishe;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControlSnack();

        initControlGoal();

        initUI();

        initListener();

    }

    private boolean isStart = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        System.out.println("onWindowFocusChanged " + hasFocus);

        gameStart();
    }



    private void initUI() {
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
        if (isStart) {
            return;
        }

        isStart = true;
        // 获取蛇ui

        // 按钮事件

        // 让蛇自动往一个方向移动

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    mRunHandler.sendEmptyMessage(controlSnack.getDirection());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        // 随机出现一个目标

        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(R.drawable.goal);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(500);
        goalView.setY(200);

        // 获取游戏场景
        RelativeLayout game_scene = findViewById(R.id.game_scene);

        // 往容器添加view
        game_scene.addView(goalView);

        collGoal = new CollGoal();
        collGoal.setGoal(getControlGoal().getGoal());
        collGoal.setView(goalView);

        collSnack = new CollSnack();
        collSnack.setSnack(getControlSnack().getSnack());
        collSnack.setView(snack_head);

        // 碰撞检测线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    mCollHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

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
        public void handleMessage(Message msg) {
            if (msg.what == Direction.DIRECTION_UP) {
                collSnack.setY(snack_head.getY() - 50);
            }

            if (msg.what == Direction.DIRECTION_RIGHT) {
                collSnack.setX(snack_head.getX() + 50);
            }

            if (msg.what == Direction.DIRECTION_DOWN) {
                collSnack.setY(snack_head.getY() + 50);
            }

            if (msg.what == Direction.DIRECTION_LEFT) {
                collSnack.setX(snack_head.getX() - 50);
            }

        }
    }

    private class CollHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // if (相撞) -> 目标消失，重新随机出现
            System.out.println("snack:" + collSnack.getRect());
            System.out.println("goal:" + collGoal.getRect());
            if (collSnack.isColl(collGoal)) {
                System.out.println("相撞");
            }

        }
    }
}