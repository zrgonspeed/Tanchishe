package top.cnzrg.tanchishe;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.security.SecureRandom;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import top.cnzrg.tanchishe.gamedata.Direction;
import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.goal.ControlGoal;
import top.cnzrg.tanchishe.goal.IControlGoalView;
import top.cnzrg.tanchishe.goal.bigbaby.BigBabyGoalView;
import top.cnzrg.tanchishe.goal.boom.BoomCollGoal;
import top.cnzrg.tanchishe.goal.boom.BoomGoalRefreshTask;
import top.cnzrg.tanchishe.goal.boom.BoomManager;
import top.cnzrg.tanchishe.goal.move.MoveGoalRunningParam;
import top.cnzrg.tanchishe.goal.prop.PropCollGoal;
import top.cnzrg.tanchishe.goal.prop.PropGoalRefreshTask;
import top.cnzrg.tanchishe.goal.shanxian.ShanXianGoalRunningParam;
import top.cnzrg.tanchishe.goal.shanxian.ShanXianGoalView;
import top.cnzrg.tanchishe.music.MusicManager;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.snack.ControlSnack;
import top.cnzrg.tanchishe.snack.IControlSnackView;
import top.cnzrg.tanchishe.snack.Snack;
import top.cnzrg.tanchishe.util.DebugUtils;
import top.cnzrg.tanchishe.util.DrawableUtils;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;
import top.cnzrg.tanchishe.util.TimeUtils;
import top.cnzrg.tanchishe.util.ToastUtils;
import top.cnzrg.tanchishe.util.WindowUtils;

public class GameSceneActivity extends Activity implements ShanXianGoalRunningParam.CreateShanXianCallBack, BoomGoalRefreshTask.CreateBoomCallBack, PropGoalRefreshTask.CreatePropCallBack, RunningParam.PropCollBoomCallBack, RunningParam.CollPropCallBack, RunningParam.GameOverCallBack, GameFlow, RunningParam.ShanXianCallBack, RunningParam.CollDetect, RunningParam.TurnToCallBack, IControlSnackView, IControlGoalView {
    private ControlSnack controlSnack;
    private ControlGoal controlGoal;

    private ShapeableImageView snack_head;

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
    private int[] sceneDrawable;

    private View layout_gameover;
    private Button bt_return;
    private TextView tv_fenshu;
    private TextView tv_time;
    private long startTime;
    private long totalTime;

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

        // 场景背景列表
        sceneDrawable = DrawableUtils.getDrawableArr(getApplicationContext(), R.array.game_scene_drawable);
        game_scene = findViewById(R.id.game_scene);
        // 场景背景,随机
        game_scene.setBackground(getDrawable(sceneDrawable[random.nextInt(sceneDrawable.length)]));

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
        mRunningParam.setGameOverCallBack(this);
        mRunningParam.setPropCollBoomCallBack(this);
        mRunningParam.setCollPropCallBack(this);
    }

    private boolean released = false;

    @Override
    protected void onResume() {
        Logger.e(TAG, "onResume()-----------------------");
        // TODO: 2021/1/12
//        if (mRunningParam.gameStatus == GameData.STATUS_PAUSE) {
//            gameResume();
//        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.e(TAG, "onPause()-----------------------");

        gamePause();
        MusicManager.getInstance().hideLrc();

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
        closeRandomTimer();

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
        // 当前是暂停状态
        if (mRunningParam.gameStatus == GameData.STATUS_PAUSE) {
            return;
        }

        // 保存运行的时间
        totalTime += System.currentTimeMillis() - startTime;

        Logger.w(TAG, "gamePause()-----------------------");
        mRunningParam.gameStatus = GameData.STATUS_PAUSE;
        // 已经播放了音乐
        if (isPlay) {
            MusicManager.getInstance().pause();
        }
    }

    public void gameResume() {
        Logger.w(TAG, "gameResume()-----------------------");
        if (gameover) {
            return;
        }

        startTime = System.currentTimeMillis();
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;
        MusicManager.getInstance().resume();
        MusicManager.getInstance().showLrc();
    }

    private boolean gameover = false;

    @Override
    public void gameOver() {
        if (!DebugUtils.debug) {
            gamePause();

            // TODO: 2021/1/30 标志gameover
            gameover = true;

            // 游戏结束
            // 显示悬浮框
            layout_gameover.setVisibility(View.VISIBLE);
            // 分数显示
            tv_fenshu.setText(String.valueOf(mRunningParam.getEatGoalCount()));
            // 时间显示
            tv_time.setText(TimeUtils.timeToString(totalTime));

            // 暂停按钮隐藏
            fab_pause.setVisibility(View.GONE);
            // 方向键隐藏
            dire_up.setVisibility(View.GONE);
            dire_down.setVisibility(View.GONE);
            dire_left.setVisibility(View.GONE);
            dire_right.setVisibility(View.GONE);
        }
        ToastUtils.showLong(getApplicationContext(), "GameOver");
    }

    public void gameQuit() {
        Logger.w(TAG, "gameQuit()-----------------------");
        randomTaskRun = false;

        mRunningParam.gameStatus = GameData.STATUS_STOP;
        mRunningParam.isRunning = false;
        mRunningParam.end();

        // 目标移动机制销毁
        MoveGoalRunningParam.getInstance().destory();
        BoomManager.getInstance().destory();

        // 闪现目标销毁
        ShanXianGoalRunningParam.getInstance().destory();

        // 目标生成机制销毁
        PropGoalRefreshTask.getInstance().destory();
        BoomGoalRefreshTask.getInstance().destory();

        // 音乐销毁
        MusicManager.getInstance().stop();

        // 线程销毁
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

        layout_gameover = findViewById(R.id.layout_gameover);
        bt_return = layout_gameover.findViewById(R.id.bt_return);
        tv_fenshu = layout_gameover.findViewById(R.id.tv_fenshu);
        tv_time = layout_gameover.findViewById(R.id.tv_time);

        snack_head = findViewById(R.id.snack_head);
        // 动态设置蛇头，有圆角边框
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shetou);
//        snack_head.setImageBitmap(bitmap);

        dire_up = findViewById(R.id.dire_up);
        dire_right = findViewById(R.id.dire_right);
        dire_down = findViewById(R.id.dire_down);
        dire_left = findViewById(R.id.dire_left);

        tv_eatCount = findViewById(R.id.tv_eatCount);

        fab_pause = findViewById(R.id.fab_pause);

        goalDrawable = DrawableUtils.getDrawableArr(getApplicationContext(), R.array.goal_drawable);
        goalBoomDrawable = DrawableUtils.getDrawableArr(getApplicationContext(), R.array.goal_boom_drawable);
    }

    private void initListener() {
        bt_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主界面
                finish();
            }
        });

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

    private boolean randomTaskRun = false;

    private Timer timer;
    private RandomCollGoalTimerTask timerTask;

    /**
     * 关闭随机物品生成计时
     */
    public void closeRandomTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private class RandomCollGoalTimerTask extends TimerTask {
        @Override
        public void run() {
            int bound = 10;
            int duile = 5;
            long time = 2000;
            randomTaskRun = true;
            while (randomTaskRun && mRunningParam != null) {
                try {
                    if (mRunningParam.gameStatus != GameData.STATUS_RUNNING) {
                    // 防止卡死，间隔一下
                    Thread.sleep(500);
                    continue;
                }

                int i = random.nextInt(bound);
                if (i == duile) {
                    // 生成随机物品
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createRandomCollGoal();
                        }
                    });
                }

                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "生成随即物品task中断");
                }
            }
        }
    }

    private void gameStart() {
        // 计时
        startTime = System.currentTimeMillis();

        // 音乐开始
//        MusicManager.getInstance().play();

        // 蛇的碰撞体设置
        createCollSnack();

        mRunningParam.isRunning = true;
        mRunningParam.gameStatus = GameData.STATUS_RUNNING;

        mRunningParam.startRefreshData();

        // 随机出现一个目标
        createCollGoal();

        // 随机物品 ？
        timer = new Timer();
        timerTask = new RandomCollGoalTimerTask();
        // 20秒后才计算生成
        // ddbug 随机物品:music
        long delay = 3000;
        timer.schedule(timerTask, delay);
    }

    // 让音乐只播放一次
    private boolean isPlay = false;

    // 生成随机物品，可能有音乐，全明星语音。
    private void createRandomCollGoal() {
        int i = random.nextInt(2);

        if (i == 0) {
            // 播放音乐
            if (!isPlay) {
                MusicManager.getInstance().play();
                isPlay = true;
                i = 1;
            }
        }

        if (i == 1) {
            // 播放全明星语音
            // 弹出全明星gif
        }
    }

    private void createCollSnack() {
        collSnackHead = new CollSnack();
        collSnackHead.setSnack(getControlSnack().getSnack());
        collSnackHead.setView(snack_head);
        collSnackHead.setXY(300f, 300f);

        snack_head.setVisibility(View.VISIBLE);
//        snack_head.setStrokeColor(ColorStateList.valueOf(R.color.black));
    }

    // 随机数安排
    private SecureRandom random = new SecureRandom();

    public void createBoomCollGoal() {
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

        BoomManager.getInstance().start(collGoal);

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
        ShanXianGoalRunningParam.getInstance().start(this, collGoal);
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
    public void createPropCollGoal() {
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

        if (mRunningParam.goalMode == 3) {
            // 降低闪现出现的概率
            if (random.nextInt(10) == 2) {
                createShanXianCollGoal();
                return;
            } else {
                mRunningParam.goalMode = random.nextInt(3);
            }
        }

        if (mRunningParam.goalMode == 0) {
            createNormalCollGoal();
        }

        if (mRunningParam.goalMode == 1) {
            createBigCollGoal();
        }

        if (mRunningParam.goalMode == 2) {
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

    @Override
    public void collisionAfter() {
        // 吃到目标后，数据刷新
        int eatGoalCount = mRunningParam.getEatGoalCount();
        tv_eatCount.setText("" + eatGoalCount);

        // 目标逐渐增多
        if (eatGoalCount == 3) {
            // 炸弹生成机制开启
            BoomGoalRefreshTask.getInstance().start(this);
        }

        if (eatGoalCount == 5) {
            // 道具生成机制开启
            PropGoalRefreshTask.getInstance().start(this);
        }
    }


    @Override
    public void propColl(CollGoal collGoal) {
        // 道具碰撞
        // 蛇头变样
        // TODO: 2021/1/21
        collSnackHead.setCurProps(collSnackHead.getCurProps() + 1);
        snack_head.setStrokeColor(ColorStateList.valueOf(Color.RED));

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
        snack_head.setStrokeColor(ColorStateList.valueOf(Color.BLACK));

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