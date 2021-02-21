package top.cnzrg.tanchishe;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.util.Logger;

public class RunningParam {
    private static RunningParam instance;

    private Thread snackRunThread;
    private Handler mRunHandler;

    private Thread collDetectThread;
    private Handler mCollHandler;

    // 开始时方向,随机
    public int direction = 0;
    public int lastDire = 0;

    public boolean isRunning = false;
    public int gameStatus = GameData.STATUS_STOP;

    private int eatGoalCount = 0;

    // 各种回调
    private TurnToCallBack mTurnToCallBack;
    private CollDetect mCollDetectCallBack;
    private GameOverCallBack mGameOverCallBack;
    private CollPropCallBack mCollPropCallBack;
    private PropCollBoomCallBack mPropCollBoomCallBack;

    public void setCollPropCallBack(CollPropCallBack mCollPropCallBack) {
        this.mCollPropCallBack = mCollPropCallBack;
    }

    public void setPropCollBoomCallBack(PropCollBoomCallBack mPropCollBoomCallBack) {
        this.mPropCollBoomCallBack = mPropCollBoomCallBack;
    }

    public int getEatGoalCount() {
        return eatGoalCount;
    }


    /**
     * 转向
     */
    interface TurnToCallBack {
        void turnTo(int dire);
    }

    /**
     * 碰撞检测
     */
    interface CollDetect {
        CollSnack getCollSnack();

        void collision(CollGoal collGoal);

        void collisionAfter();

        List<CollGoal> getCollGoals();
    }

    /**
     * 闪现
     */
    interface ShanXianCallBack {
        void shanxian(CollGoal collGoal);
    }

    /**
     * 碰到炸弹后的回调
     */
    interface GameOverCallBack {
        void boomColl(CollGoal collGoal);
    }

    /**
     * 碰到道具后的回调
     */
    interface CollPropCallBack {
        void propColl(CollGoal collGoal);
    }

    /**
     * 带着道具碰炸弹
     */
    interface PropCollBoomCallBack {
        void propCollBoom(CollGoal collGoal);
    }

    private RunningParam() {
    }

    public void startRefreshData() {
        mRunHandler = new RunHandler(this);
        mCollHandler = new CollHandler(this);

        // 蛇运动
        snackRunThread = new SnackRunThread();
        snackRunThread.start();

        // 碰撞检测线程
        collDetectThread = new CollDetectThread();
        collDetectThread.start();
    }

    public void end() {
        if (isRunning) {
            isRunning = false;
        }
        interrupted();

        mCollHandler.removeCallbacksAndMessages(null);
        mRunHandler.removeCallbacksAndMessages(null);

        mRunHandler = null;
        mCollHandler = null;

        mTurnToCallBack = null;
        mCollDetectCallBack = null;
        mGameOverCallBack = null;
        mPropCollBoomCallBack = null;
        mCollPropCallBack = null;

        instance = null;
    }

    public synchronized void interrupted() {
        if (snackRunThread != null) {
            snackRunThread.interrupt();
            snackRunThread = null;
        }
        if (collDetectThread != null) {
            collDetectThread.interrupt();
            collDetectThread = null;
        }
//        ThreadManager.getInstance().destory();
    }

    public void setCollDetectCallBack(CollDetect collDetect) {
        this.mCollDetectCallBack = collDetect;
    }

    public void setTurnToCallBack(TurnToCallBack mTurnToCallBack) {
        this.mTurnToCallBack = mTurnToCallBack;
    }

    public void setGameOverCallBack(GameOverCallBack callBack) {
        mGameOverCallBack = callBack;
    }

    private class SnackRunThread extends Thread {

        @Override
        public void run() {
            // 随机方向
            direction = new Random().nextInt(4) + 1;

            while (isRunning) {
                try {
                    if (gameStatus != GameData.STATUS_RUNNING) {
                        // 防止卡死，间隔一下
                        Thread.sleep(500);
                        continue;
                    }

                    mRunHandler.sendEmptyMessage(direction);
                    Thread.sleep(GameData.SNACK_MOVE_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "运行线程中断");
                    break;
                }
            }
        }
    }


    private static class RunHandler extends Handler {
        private WeakReference<RunningParam> weakReference;
        private RunningParam mRunningParam;

        RunHandler(RunningParam mRunningParam) {
            if (mRunningParam != null) {
                weakReference = new WeakReference<>(mRunningParam);
            }
        }

        @Override
        synchronized public void handleMessage(Message msg) {
            mRunningParam = weakReference.get();
            if (mRunningParam == null) {
                return;
            }

            mRunningParam.mTurnToCallBack.turnTo(msg.what);
        }
    }

    public static String TAG = "RunningParam";

    private class CollDetectThread extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    if (gameStatus != GameData.STATUS_RUNNING) {
                        // 防止卡死，间隔一下
                        Thread.sleep(500);
                        continue;
                    }

                    mCollHandler.sendEmptyMessage(0);

                    Thread.sleep(GameData.COLL_GOAL_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "碰撞线程中断");
                    break;
                }
            }
        }
    }


    private static class CollHandler extends Handler {
        private WeakReference<RunningParam> weakReference;
        private RunningParam mRunningParam;

        boolean flag = false;

        CollHandler(RunningParam mRunningParam) {
            if (mRunningParam != null) {
                weakReference = new WeakReference<>(mRunningParam);
            }
        }

        @Override
        synchronized public void handleMessage(Message msg) {
            mRunningParam = weakReference.get();
            if (mRunningParam == null) {
                return;
            }

            // if (相撞) -> 目标消失，重新随机出现

//            CollGoal collGoal = mRunningParam.mCollDetectCallBack.getCollGoal();
            List<CollGoal> collGoals = mRunningParam.mCollDetectCallBack.getCollGoals();
            CollSnack collSnack = mRunningParam.mCollDetectCallBack.getCollSnack();

            if (collSnack == null || collGoals == null || collGoals.size() == 0) {
                return;
            }

            if (flag == true)
                return;

            for (int i = 0; i < collGoals.size(); i++) {
                CollGoal collGoal = collGoals.get(i);
                if (collGoal.isOver()) {
                    continue;
                }

                if (collSnack.isColl(collGoal)) {
                    flag = true;
                    collGoal.setOver(true);
                    if (collGoal.isBoom()) {
                        if (collSnack.getCurProps() > 0) {
                            // 多个虎头道具只有一次效果
                            mRunningParam.mPropCollBoomCallBack.propCollBoom(collGoal);
                        } else {
                            mRunningParam.mGameOverCallBack.boomColl(collGoal);
                        }
                    } else if (collGoal.isProp()) {
                        // 蛇头增加吃炸弹效果
                        mRunningParam.mCollPropCallBack.propColl(collGoal);
                    } else {
                        // 吃到目标数计数
                        mRunningParam.eatGoalCount++;
                        mRunningParam.mCollDetectCallBack.collision(collGoal);
                        mRunningParam.mCollDetectCallBack.collisionAfter();
                    }
                    flag = false;
                }
            }
        }
    }

    public int goalMode = 0;

    public static RunningParam getInstance() {
        if (instance == null) {
            synchronized (RunningParam.class) {
                if (instance == null) {
                    instance = new RunningParam();
                }
            }
        }

        return instance;
    }
}
