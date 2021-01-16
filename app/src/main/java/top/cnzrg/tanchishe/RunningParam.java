package top.cnzrg.tanchishe;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.util.Logger;

public class RunningParam {
    private static RunningParam instance;

    private Thread snackRunThread;
    private Handler mRunHandler;

    private Thread collDetectThread;
    private Handler mCollHandler;

    public int direction = 1;
    public int lastDire = 0;

    public boolean isRunning = false;
    public int gameStatus = GameData.STATUS_STOP;

    private TurnToCallBack mTurnToCallBack;
    private CollDetect mCollDetectCallBack;

    private int eatGoalCount = 0;

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

        CollGoal getCollGoal();

        void collision();

        void collisionAfter();
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
    }

    public void setCollDetectCallBack(CollDetect collDetect) {
        this.mCollDetectCallBack = collDetect;
    }

    public void setTurnToCallBack(TurnToCallBack mTurnToCallBack) {
        this.mTurnToCallBack = mTurnToCallBack;
    }

    private class SnackRunThread extends Thread {

        @Override
        public void run() {
            while (isRunning) {
                if (gameStatus != GameData.STATUS_RUNNING) {
                    continue;
                }

                mRunHandler.sendEmptyMessage(direction);
                try {
                    Thread.sleep(GameData.SNACK_MOVE_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "运行线程中断");
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
                if (gameStatus != GameData.STATUS_RUNNING) {
                    continue;
                }

                mCollHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(GameData.COLL_GOAL_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "碰撞线程中断");
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

            CollGoal collGoal = mRunningParam.mCollDetectCallBack.getCollGoal();
            CollSnack collSnack = mRunningParam.mCollDetectCallBack.getCollSnack();

            if (collSnack == null || collGoal == null) {
                return;
            }

            if (flag == true)
                return;

            if (collSnack.isColl(collGoal)) {
                flag = true;
                // 吃到目标数计数
                mRunningParam.eatGoalCount++;
                mRunningParam.mCollDetectCallBack.collision();
                mRunningParam.mCollDetectCallBack.collisionAfter();
                flag = false;
            }
        }
    }

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
