package top.cnzrg.tanchishe;

import android.os.Handler;
import android.os.Message;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;

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
        mRunHandler = new RunHandler();
        mCollHandler = new CollHandler();
    }

    public void startRefreshData() {
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
        mCollHandler.removeCallbacksAndMessages(null);
        mRunHandler.removeCallbacksAndMessages(null);

        mTurnToCallBack = null;
        mCollDetectCallBack = null;

        interrupted();
    }

    public synchronized void interrupted() {
        try {
            if (snackRunThread != null) {
                snackRunThread.interrupt();
                snackRunThread = null;
            }
            if (collDetectThread != null) {
                collDetectThread.interrupt();
                collDetectThread = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                    e.printStackTrace();
                }
            }
        }
    }


    private class RunHandler extends Handler {
        @Override
        synchronized public void handleMessage(Message msg) {
            mTurnToCallBack.turnTo(msg.what);
        }
    }

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
                    e.printStackTrace();
                }
            }
        }
    }


    private class CollHandler extends Handler {
        boolean flag = false;

        @Override
        synchronized public void handleMessage(Message msg) {
            // if (相撞) -> 目标消失，重新随机出现

            CollGoal collGoal = mCollDetectCallBack.getCollGoal();
            CollSnack collSnack = mCollDetectCallBack.getCollSnack();

            if (collSnack == null || collGoal == null) {
                return;
            }

            if (flag == true)
                return;

            if (collSnack.isColl(collGoal)) {
                flag = true;
                // 吃到目标数计数
                eatGoalCount++;
                mCollDetectCallBack.collision();
                mCollDetectCallBack.collisionAfter();
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
