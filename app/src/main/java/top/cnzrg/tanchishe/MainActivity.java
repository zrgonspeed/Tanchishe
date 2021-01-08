package top.cnzrg.tanchishe;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends Activity implements IControlSnackView {
    private ControlSnack controlSnack;
    private ImageView snack_head;
    private Handler mRunHandler;
    private FloatingActionButton dire_up;
    private FloatingActionButton dire_right;
    private FloatingActionButton dire_down;
    private FloatingActionButton dire_left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControlSnack();

        initUI();

        initListener();

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
                        Thread.sleep(1000);
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

    @Override
    public ControlSnack getControlSnack() {
        return ControlSnack.getInstance();
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
                snack_head.setY(snack_head.getY() - 50);
            }

            if (msg.what == Direction.DIRECTION_RIGHT) {
                snack_head.setX(snack_head.getX() + 50);
            }

            if (msg.what == Direction.DIRECTION_DOWN) {
                snack_head.setY(snack_head.getY() + 50);
            }

            if (msg.what == Direction.DIRECTION_LEFT) {
                snack_head.setX(snack_head.getX() - 50);
            }

        }
    }
}