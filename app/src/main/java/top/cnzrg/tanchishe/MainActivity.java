package top.cnzrg.tanchishe;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.SecureRandom;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.goal.ControlGoal;
import top.cnzrg.tanchishe.goal.Goal;
import top.cnzrg.tanchishe.goal.IControlGoalView;
import top.cnzrg.tanchishe.param.Direction;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.snack.ControlSnack;
import top.cnzrg.tanchishe.snack.IControlSnackView;
import top.cnzrg.tanchishe.snack.Snack;
import top.cnzrg.tanchishe.util.DebugUtils;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ToastUtils;

public class MainActivity extends Activity implements RunningParam.CollDetect, RunningParam.TurnToCallBack, IControlSnackView, IControlGoalView {
    private ControlSnack controlSnack;
    private ControlGoal controlGoal;

    private ImageView snack_head;

    private FloatingActionButton dire_up;
    private FloatingActionButton dire_right;
    private FloatingActionButton dire_down;
    private FloatingActionButton dire_left;


    private CollGoal collGoal;
    private CollSnack collSnackHead;
    private CollSnack lastBody;
    private ConstraintLayout game_scene;
    private RunningParam mRunningParam;

    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TAG = getClass().getSimpleName();
        Logger.e(TAG, "onCreate()-----------------------");

        mRunningParam = RunningParam.getInstance();
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

        mRunningParam.setTurnToCallBack(this);
        mRunningParam.setCollDetectCallBack(this);
    }

    @Override
    protected void onResume() {
        Logger.e(TAG, "onResume()-----------------------");
        // TODO: 2021/1/12
        gameResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.e(TAG, "onPause()-----------------------");

        gamePause();

        if (isFinishing()) {
            gameQuit();
            release();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Logger.e(TAG, "onDestroy()-----------------------");

        gamePause();
        gameQuit();

        release();

        super.onDestroy();
    }

    private void release() {
        Logger.i(TAG,"释放资源");

        collGoal = null;
        collSnackHead = null;
        lastBody = null;
        snack_head = null;

        this.dire_down = null;
        this.dire_left = null;
        this.dire_right = null;
        this.dire_up = null;

        controlSnack.unRegisterSnack();
        controlGoal.unRegisterGoal();
    }

    private void gamePause() {
        Logger.w(TAG, "gamePause()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_PAUSE;
    }

    private void gameResume() {
        Logger.w(TAG, "gameResume()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;
    }

    private void gameQuit() {
        Logger.w(TAG, "gameQuit()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_STOP;
        mRunningParam.isRunning = false;
        mRunningParam.end();
    }

    private boolean isFirst = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Logger.e(TAG, "onWindowFocusChanged()------" + hasFocus);

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

        Logger.d(TAG, "SCENE_HEIGHT " + height + ", SCENE_WIDTH " + width);

        ToastUtils.showLong(this, "H " + height + " W " + width + " S " + snack_head.getWidth());

        gameStart();
        // 网格线添加
        DebugUtils.addGridLine(this, game_scene);
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

        dire_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRunningParam.direction = Direction.DIRECTION_UP;
            }
        });

        dire_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRunningParam.direction = Direction.DIRECTION_RIGHT;
            }
        });

        dire_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRunningParam.direction = Direction.DIRECTION_DOWN;
            }
        });

        dire_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRunningParam.direction = Direction.DIRECTION_LEFT;
            }
        });
    }


    private void gameStart() {
        // 随机出现一个目标
        createCollGoal();

        // 蛇的碰撞体设置
        createCollSnack();

        mRunningParam.isRunning = true;
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;

        mRunningParam.startRefreshData();
    }

    private void createCollSnack() {
        collSnackHead = new CollSnack();
        collSnackHead.setSnack(getControlSnack().getSnack());
        collSnackHead.setView(snack_head);
    }

    SecureRandom random = new SecureRandom();

    private void createCollGoal() {
        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(R.drawable.goal);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        collGoal = new CollGoal();
        collGoal.setGoal(getControlGoal().getGoal());
        collGoal.setView(goalView);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + goalView.getX() + " - " + goalView.getY());
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
        Logger.d(TAG, "turnUP()");

        int newY = (int) (snack_head.getY() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY <= 0) {
            Logger.i("game over DIRECTION_UP");
            newY = 0;
        }
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    @Override
    public void turnLeft() {
        Logger.d(TAG, "turnLeft()");

        int newX = (int) (snack_head.getX() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX <= 0) {
            Logger.i("game over DIRECTION_LEFT");
            newX = 0;
        }
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnRight() {
        Logger.d(TAG,"turnRight()");

        int newX = (int) (snack_head.getX() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
            Logger.i("game over DIRECTION_RIGHT");
            newX = GameData.SCENE_WIDTH - collSnackHead.getView().getWidth();
        }
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnDown() {
        Logger.d(TAG,"turnDown()");

        int newY = (int) (snack_head.getY() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY >= GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
            Logger.i("game over DIRECTION_DOWN");
            newY = GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight();
        }
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    @Override
    public void turnTo(int dire) {
        if (dire == Direction.DIRECTION_UP) {
            if (collSnackHead.getView().getY() <= 0) {
                return;
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_DOWN) {
                turnDown();
                return;
            }

            turnUP();

        }

        if (dire == Direction.DIRECTION_RIGHT) {
            if (collSnackHead.getView().getX() >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
                return;
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_LEFT) {
                turnLeft();
                return;
            }

            turnRight();
        }

        if (dire == Direction.DIRECTION_DOWN) {
            if (collSnackHead.getView().getY() >= GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
                return;
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_UP) {
                turnUP();
                return;
            }

            turnDown();
        }

        if (dire == Direction.DIRECTION_LEFT) {
            if (collSnackHead.getView().getX() <= 0) {
                return;
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_RIGHT) {
                turnRight();
                return;
            }

            turnLeft();
        }

        mRunningParam.lastDire = dire;
    }


    @Override
    public CollSnack getCollSnack() {
        return collSnackHead;
    }

    @Override
    public CollGoal getCollGoal() {
        return collGoal;
    }

    private int a = 0;

    @Override
    public void collision() {
        Logger.i(TAG, "相撞");
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

    }
}