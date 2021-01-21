package top.cnzrg.tanchishe;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.goal.ControlGoal;
import top.cnzrg.tanchishe.goal.IControlGoalView;
import top.cnzrg.tanchishe.goal.other.BigBabyGoalView;
import top.cnzrg.tanchishe.goal.other.BoomMoveGoalRunningParam;
import top.cnzrg.tanchishe.goal.other.BoomCollGoal;
import top.cnzrg.tanchishe.goal.other.MoveGoalRunningParam;
import top.cnzrg.tanchishe.goal.other.PropCollGoal;
import top.cnzrg.tanchishe.goal.other.ShanXianGoalView;
import top.cnzrg.tanchishe.param.Direction;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.snack.ControlSnack;
import top.cnzrg.tanchishe.snack.IControlSnackView;
import top.cnzrg.tanchishe.snack.Snack;
import top.cnzrg.tanchishe.snack.SnackHeadImageView;
import top.cnzrg.tanchishe.util.DebugUtils;
import top.cnzrg.tanchishe.util.DrawableUtils;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;
import top.cnzrg.tanchishe.util.ToastUtils;
import top.cnzrg.tanchishe.util.WindowUtils;

public class GameSceneActivity extends Activity implements RunningParam.PropCollBoomCallBack, RunningParam.CollPropCallBack, RunningParam.GameOverCallBack, GameFlow, RunningParam.ShanXianCallBack, RunningParam.CollDetect, RunningParam.TurnToCallBack, IControlSnackView, IControlGoalView {
    private ControlSnack controlSnack;
    private ControlGoal controlGoal;

    private SnackHeadImageView snack_head;

    private FloatingActionButton dire_up;
    private FloatingActionButton dire_right;
    private FloatingActionButton dire_down;
    private FloatingActionButton dire_left;

    private FloatingActionButton fab_pause;

    private CollSnack collSnackHead;
    private CollSnack lastBody;
    private ConstraintLayout game_scene;
    private RunningParam mRunningParam;

    private String TAG;
    private TextView tv_eatCount;

    private int[] goalDrawable;
    private int[] goalBoomDrawable;

    /**
     * 防止横屏闪退
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_scene);

        //隐藏虚拟按键，并且全屏
        WindowUtils.hideBottomUIMenu(getWindow());

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
        mRunningParam.setShanXianCallBack(this);
        mRunningParam.setGameOverCallBack(this);
        mRunningParam.setPropCollBoomCallBack(this);
        mRunningParam.setCollPropCallBack(this);
    }

    private boolean released = false;

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
            released = true;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Logger.e(TAG, "onDestroy()-----------------------");

        if (released) {
            super.onDestroy();
            return;
        }
        gamePause();
        gameQuit();

        release();
        super.onDestroy();
    }

    private void release() {
        Logger.i(TAG, "释放资源");

        collSnackHead = null;
        lastBody = null;
        snack_head = null;

        this.dire_down = null;
        this.dire_left = null;
        this.dire_right = null;
        this.dire_up = null;

        controlSnack.unRegisterSnack();

        controlSnack.destory();
        controlGoal.destory();

        controlSnack = null;
        controlGoal = null;

        mRunningParam = null;

        ToastUtils.destory();

        game_scene.removeAllViews();
    }

    public void gamePause() {
        Logger.w(TAG, "gamePause()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_PAUSE;
    }

    public void gameResume() {
        Logger.w(TAG, "gameResume()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;
    }

    @Override
    public void gameOver() {
        if (!DebugUtils.debug) {
            gamePause();
        }
        ToastUtils.showLong(getApplicationContext(), "GameOver");
    }

    public void gameQuit() {
        Logger.w(TAG, "gameQuit()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_STOP;
        mRunningParam.isRunning = false;
        mRunningParam.end();

        MoveGoalRunningParam.getInstance().destory();
        BoomMoveGoalRunningParam.getInstance().destory();

        ThreadManager.getInstance().destory();
    }

    private boolean isFirst = true;

    @SuppressLint("ResourceAsColor")
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

        GameData.SCENE_HEIGHT = height / 100 * 100;
        GameData.SCENE_WIDTH = width / 100 * 100;

        Logger.d(TAG, "SCENE_HEIGHT " + height + ", SCENE_WIDTH " + width);

        ToastUtils.showLong(this, "H " + height + " W " + width + " S " + snack_head.getWidth());

        gameStart();
        // 网格线添加
        DebugUtils.addGridLine(this, game_scene);

        // 填充屏幕右边和下边
        WindowUtils.fillRightAndBottom(this, game_scene, width, height);
    }


    private void initUI() {
        game_scene = findViewById(R.id.game_scene);

        snack_head = findViewById(R.id.snack_head);
        // 动态设置蛇头，有圆角边框
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shetou);
        snack_head.setImageBitmap(bitmap);

        dire_up = findViewById(R.id.dire_up);
        dire_right = findViewById(R.id.dire_right);
        dire_down = findViewById(R.id.dire_down);
        dire_left = findViewById(R.id.dire_left);

        tv_eatCount = findViewById(R.id.tv_eatCount);

        fab_pause = findViewById(R.id.fab_pause);

        goalDrawable = DrawableUtils.getGoalDrawable(getApplicationContext(), R.array.goal_drawable);
        goalBoomDrawable = DrawableUtils.getGoalDrawable(getApplicationContext(), R.array.goal_boom_drawable);
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

        fab_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning()) {
                    gamePause();
                } else {
                    gameResume();
                }
            }
        });
    }

    private boolean isRunning() {
        return mRunningParam.gameStatus == GameData.STATUS_RUNNING;
    }

    private void gameStart() {
        // 蛇的碰撞体设置
        createCollSnack();

        mRunningParam.isRunning = true;
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;

        mRunningParam.startRefreshData();

        // 随机出现一个目标
        createCollGoal();
    }

    private void createCollSnack() {
        collSnackHead = new CollSnack();
        collSnackHead.setSnack(getControlSnack().getSnack());
        collSnackHead.setView(snack_head);
        collSnackHead.setXY(300f, 300f);

        snack_head.setVisibility(View.VISIBLE);
    }

    // 随机数安排
    private SecureRandom random = new SecureRandom();

    private void createBoomMoveCollGoal() {
        Logger.i(TAG, "当前目标类型: 移动炸弹");

        //------------------------移动BoomGoal
        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(goalBoomDrawable[random.nextInt(goalBoomDrawable.length)]);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        BoomCollGoal collGoal = getControlGoal().newBoomCollGoal(goalView);

        BoomMoveGoalRunningParam.getInstance().start(collGoal);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + goalView.getX() + " - " + goalView.getY());
    }

    private void createMoveCollGoal() {
        Logger.i(TAG, "当前目标类型: 移动");

        //------------------------移动goal
        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(goalDrawable[random.nextInt(8)]);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_MOVE_WIDTH_HEIGHT, GameData.GOAL_MOVE_WIDTH_HEIGHT));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        CollGoal collGoal = getControlGoal().newCollGoal(goalView);

        MoveGoalRunningParam.getInstance().start(collGoal);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + goalView.getX() + " - " + goalView.getY());
    }


    private void createShanXianCollGoal() {
        Logger.i(TAG, "当前目标类型: 闪现");

        // 闪现---------------------------------
        ShanXianGoalView shanXianGoalView = new ShanXianGoalView(this);
        shanXianGoalView.setImageResource(goalDrawable[random.nextInt(8)]);
        shanXianGoalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // 设为负数表示不在屏幕上显示，避免一开始出现在屏幕上突然闪到下个左边
        shanXianGoalView.setX(-200);
        shanXianGoalView.setY(-200);
        shanXianGoalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));

        game_scene.addView(shanXianGoalView);

        // 碰撞目标 设置
        CollGoal collGoal = getControlGoal().newCollGoal(shanXianGoalView);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + shanXianGoalView.getX() + " - " + shanXianGoalView.getY());

        // 闪现线程
        mRunningParam.startShanXian(collGoal);

    }

    private void createBigCollGoal() {
        Logger.i(TAG, "当前目标类型: 大宝贝");

        // 大图片----------------------------------
        BigBabyGoalView bigBabyGoalView = new BigBabyGoalView(this);
        bigBabyGoalView.setImageResource(goalDrawable[random.nextInt(8)]);
        bigBabyGoalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bigBabyGoalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        bigBabyGoalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));
        bigBabyGoalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_BIG_WIDTH_HEIGHT, GameData.GOAL_BIG_WIDTH_HEIGHT));

        game_scene.addView(bigBabyGoalView);

        // 碰撞目标 设置
        CollGoal collGoal = getControlGoal().newCollGoal(bigBabyGoalView);

        //动画效果参数直接定义
        Animation animation = new AlphaAnimation(0.1f, 1.0f);
        animation.setDuration(3000);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (collGoal.isOver() || mRunningParam == null) {
                    return;
                }
                game_scene.removeView(bigBabyGoalView);
                collGoal.setOver(true);
                createCollGoal();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        bigBabyGoalView.startAnimation(animation);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + bigBabyGoalView.getX() + " - " + bigBabyGoalView.getY());
    }


    private void createNormalCollGoal() {
        Logger.i(TAG, "当前目标类型: 普通");

        //------------------------常规goal
        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(goalDrawable[random.nextInt(8)]);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        CollGoal collGoal = getControlGoal().newCollGoal(goalView);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + goalView.getX() + " - " + goalView.getY());
    }

    /**
     * 生成道具
     */
    private void createPropCollGoal() {
        Logger.i(TAG, "当前目标类型: 道具");

        // 目标图片
        ImageView goalView = new ImageView(this);
        goalView.setImageResource(R.drawable.shetou);
        goalView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        goalView.setX(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1));
        goalView.setY(random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));

        goalView.setLayoutParams(new ConstraintLayout.LayoutParams(GameData.GOAL_WIDTH_HEIGHT, GameData.GOAL_WIDTH_HEIGHT));

        // 往容器添加view
        game_scene.addView(goalView);

        // 碰撞目标 设置
        PropCollGoal collGoal = getControlGoal().newPropCollGoal(goalView);

        Logger.i(TAG, "createCollGoal()------目标生成:" + collGoal.getName() + "  " + goalView.getX() + " - " + goalView.getY());
    }

    private void createCollGoal() {
        // TODO: 2021/1/19
        mRunningParam.goalMode = random.nextInt(4);

        if (mRunningParam.goalMode == 0) {
            createNormalCollGoal();
        }

        if (mRunningParam.goalMode == 1) {
            createBigCollGoal();
        }

        if (mRunningParam.goalMode == 2) {
            createShanXianCollGoal();
        }

        if (mRunningParam.goalMode == 3) {
            createMoveCollGoal();
        }

//        if (mRunningParam.goalMode == 4) {
//            createBoomMoveCollGoal();
//        }
    }

    private void initControlSnack() {
        controlSnack = getControlSnack();
        controlSnack.registerSnack(new Snack());
        controlSnack.setContext(getApplicationContext());
        controlSnack.setView(this);
    }

    private void initControlGoal() {
        controlGoal = getControlGoal();
        controlGoal.setContext(getApplicationContext());
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

        if (snack_head.getY() < 0) {
            gameOver();
            return;
        }

        int newY = (int) (snack_head.getY() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY < 0) {
            gameOver();
            newY = 0;
        }
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    @Override
    public void turnLeft() {
        Logger.d(TAG, "turnLeft()");

        if (snack_head.getX() < 0) {
            gameOver();
            return;
        }

        int newX = (int) (snack_head.getX() - GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX < 0) {
            gameOver();
            newX = 0;
        }
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnRight() {
        Logger.d(TAG, "turnRight()");

        if (snack_head.getX() >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
            gameOver();
            return;
        }

        int newX = (int) (snack_head.getX() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newX > GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
            gameOver();
            newX = GameData.SCENE_WIDTH - collSnackHead.getView().getWidth();
        }
        collSnackHead.setXY(newX, collSnackHead.getView().getY());
    }

    @Override
    public void turnDown() {
        Logger.d(TAG, "turnDown()");

        if (snack_head.getY() > GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
            gameOver();
            return;
        }

        int newY = (int) (snack_head.getY() + GameData.SNACK_MOVE_DIST_INTERVAL);
        if (newY > GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
            gameOver();
            newY = GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight();
        }
        collSnackHead.setXY(collSnackHead.getView().getX(), newY);
    }

    @Override
    public void turnTo(int dire) {
        if (dire == Direction.DIRECTION_UP) {
            if (collSnackHead.getView().getY() <= 0) {
                if (mRunningParam.lastDire != Direction.DIRECTION_UP) {
                    turnTo(mRunningParam.lastDire);
                    return;
                }
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_DOWN) {
                turnDown();
                return;
            }

            turnUP();

        }

        if (dire == Direction.DIRECTION_RIGHT) {
            if (collSnackHead.getView().getX() >= GameData.SCENE_WIDTH - collSnackHead.getView().getWidth()) {
                if (mRunningParam.lastDire != Direction.DIRECTION_RIGHT) {
                    turnTo(mRunningParam.lastDire);
                    return;
                }
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_LEFT) {
                turnLeft();
                return;
            }

            turnRight();
        }

        if (dire == Direction.DIRECTION_DOWN) {
            if (collSnackHead.getView().getY() >= GameData.SCENE_HEIGHT - collSnackHead.getView().getHeight()) {
                if (mRunningParam.lastDire != Direction.DIRECTION_DOWN) {
                    turnTo(mRunningParam.lastDire);
                    return;
                }
            }

            if (mRunningParam.lastDire == Direction.DIRECTION_UP) {
                turnUP();
                return;
            }

            turnDown();
        }

        if (dire == Direction.DIRECTION_LEFT) {
            if (collSnackHead.getView().getX() <= 0) {
                if (mRunningParam.lastDire != Direction.DIRECTION_LEFT) {
                    turnTo(mRunningParam.lastDire);
                    return;
                }
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
    public void collision(CollGoal collGoal) {
        Logger.i(TAG, "相撞");
        // 移除图片动画
        ImageView view = collGoal.getView();
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.cancel();
            view.clearAnimation();
            animation = null;
        }

        //动画效果参数直接定义
        Animation animation2 = new AlphaAnimation(1.0f, 0.1f);
        animation2.setDuration(200);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAnimation(null);
                game_scene.removeView(view);
                Logger.e("collison end -+----------------------");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation2);

        // 场景移除图片
//        game_scene.removeView(collGoal.getView());

        createCollGoal();

        // 蛇身增加一块
        CollSnack body = new CollSnack();
        // snack实体还是通用
        body.setSnack(getControlSnack().getSnack());

        // 身体图片
        ImageView bodyView = new ImageView(GameSceneActivity.this);
        bodyView.setImageDrawable(collGoal.getView().getDrawable());
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

    /**
     * 获取场景炸弹列表
     *
     * @return
     */
    public List<BoomCollGoal> getBoomCollGoals() {
        List<BoomCollGoal> collBoomGoals = new ArrayList<>();

        List<CollGoal> collGoals = getControlGoal().getCollGoals();
        for (CollGoal collGoal : collGoals) {
            if (collGoal.isBoom() && !collGoal.isOver()) {
                collBoomGoals.add((BoomCollGoal) collGoal);
            }
        }
        return collBoomGoals;
    }

    /**
     * 获取场景道具列表
     *
     * @return
     */
    public List<PropCollGoal> getPropCollGoals() {
        List<PropCollGoal> propCollGoals = new ArrayList<>();

        List<CollGoal> collGoals = getControlGoal().getCollGoals();
        for (CollGoal collGoal : collGoals) {
            if (collGoal.isProp() && !collGoal.isOver()) {
                propCollGoals.add((PropCollGoal) collGoal);
            }
        }
        return propCollGoals;
    }

    @Override
    public void collisionAfter() {
        // 吃到目标后，数据刷新
        int eatGoalCount = mRunningParam.getEatGoalCount();
        tv_eatCount.setText("" + eatGoalCount);

        // 目标逐渐增多
        if (eatGoalCount == 5) {
            // 炸弹生成机制开启
            BoomGoalRefreshThread thread = new BoomGoalRefreshThread();
            ThreadManager.getInstance().addThread(thread);
            thread.start();
        }

        if (eatGoalCount == 10) {
            // 道具生成机制开启
            PropGoalRefreshThread thread = new PropGoalRefreshThread();
            ThreadManager.getInstance().addThread(thread);
            thread.start();
        }
    }



    @Override
    public void propColl(CollGoal collGoal) {
        // 道具碰撞
        // 蛇头变样
        // TODO: 2021/1/21
        collSnackHead.setCurProps(collSnackHead.getCurProps() + 1);

        Logger.i(TAG, "吃到道具");
        // 移除图片动画
        ImageView view = collGoal.getView();
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.cancel();
            view.clearAnimation();
            animation = null;
        }

        //动画效果参数直接定义
        Animation animation2 = new AlphaAnimation(1.0f, 0.1f);
        animation2.setDuration(200);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAnimation(null);
                game_scene.removeView(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation2);
    }

    @Override
    public void propCollBoom(CollGoal collGoal) {
        // 有道具去碰撞炸弹
        Logger.i(TAG, "有道具去碰撞炸弹");

        // 道具只能用一次哈
        collSnackHead.setCurProps(0);

        // 移除图片动画
        ImageView view = collGoal.getView();
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.cancel();
            view.clearAnimation();
            animation = null;
        }

        //动画效果参数直接定义
        Animation animation2 = new AlphaAnimation(1.0f, 0.1f);
        animation2.setDuration(200);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAnimation(null);
                game_scene.removeView(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation2);
    }

    /**
     * 道具生成线程
     */
    private class PropGoalRefreshThread extends Thread {
        @Override
        public void run() {
            try {
                while (mRunningParam != null && mRunningParam.isRunning) {
                    if (mRunningParam.gameStatus != GameData.STATUS_RUNNING) {
                        continue;
                    }

                    List<PropCollGoal> collPropGoals = getPropCollGoals();

                    if (collPropGoals.size() == 0) {
                        // 4分之1几率生成道具
                        if (random.nextInt(4) == 3) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createPropCollGoal();
                                }
                            });
                        }
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Logger.w(TAG, "道具生成线程中断");
            }
        }
    }


    /**
     * 炸弹生成线程
      */
    private class BoomGoalRefreshThread extends Thread {
        @Override
        public void run() {
            try {
                while (mRunningParam != null && mRunningParam.isRunning) {
                    if (mRunningParam.gameStatus != GameData.STATUS_RUNNING) {
                        continue;
                    }

                    List<BoomCollGoal> collBoomGoals = getBoomCollGoals();
                    // 判断条件,场景上同时存在的炸弹
                    if (collBoomGoals.size() == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createBoomMoveCollGoal();
                            }
                        });
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Logger.w(TAG, "炸弹生成线程中断");
            }
        }
    }

    @Override
    public List<CollGoal> getCollGoals() {
        return getControlGoal().getCollGoals();
    }

    @Override
    public void shanxian(CollGoal collGoal) {
        collGoal.setXY(random.nextInt(GameData.SCENE_WIDTH - GameData.GOAL_WIDTH_HEIGHT + 1), random.nextInt(GameData.SCENE_HEIGHT - GameData.GOAL_WIDTH_HEIGHT + 1));
    }

    @Override
    public void boomColl(CollGoal collGoal) {
        Logger.e(TAG, "撞到炸弹！！！！！！！！！！！");

        // 移除图片动画
        ImageView view = collGoal.getView();
        Animation animation = view.getAnimation();
        if (animation != null) {
            animation.cancel();
            view.clearAnimation();
            animation = null;
        }

        //动画效果: 炸弹一闪一闪
        Animation animation2 = new AlphaAnimation(1.0f, 0.3f);
        animation2.setDuration(700);
        animation2.setRepeatCount(Animation.INFINITE);
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAnimation(null);
                game_scene.removeView(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(animation2);
        gameOver();
    }
}