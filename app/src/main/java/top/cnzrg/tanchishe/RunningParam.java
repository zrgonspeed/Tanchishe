package top.cnzrg.tanchishe;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.snack.CollSnack;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;

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

    // 闪现
    private ShanXianCallBack mShanXianCallBack;
    private Handler mShanXianCollHandler;

    private int eatGoalCount = 0;
    private GameOverCallBack mGameOverCallBack;

    public int getEatGoalCount() {
        return eatGoalCount;
    }

    /**
     * 开始闪现
     * @param collGoal
     */
    public void startShanXian(CollGoal collGoal) {
        ShanXianCollGoalThread shanXianCollGoalThread = new ShanXianCollGoalThread(collGoal);
        ThreadManager.getInstance().addThread(shanXianCollGoalThread);
        shanXianCollGoalThread.start();
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

    interface ShanXianCallBack {
        void shanxian(CollGoal collGoal);
    }

    interface GameOverCallBack {
        void boomColl(CollGoal collGoal);
    }

    private RunningParam() {
    }

    public void startRefreshData() {
        mRunHandler = new RunHandler(this);
        mCollHandler = new CollHandler(this);
        mShanXianCollHandler = new ShanXianCollHandler(this);

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
        mShanXianCollHandler.removeCallbacks(null);

        mRunHandler = null;
        mCollHandler = null;
        mShanXianCollHandler = null;

        mTurnToCallBack = null;
        mCollDetectCallBack = null;
        mShanXianCallBack = null;
        mGameOverCallBack = null;

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

    public void setShanXianCallBack(ShanXianCallBack mShanXianCallBack) {
        this.mShanXianCallBack = mShanXianCallBack;
    }

    public void setGameOverCallBack(GameOverCallBack callBack) {
        mGameOverCallBack = callBack;
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
                if (gameStatus != GameData.STATUS_RUNNING) {
                    continue;
                }

                mCollHandler.sendEmptyMessage(0);
                try {
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
                        mRunningParam.mGameOverCallBack.boomColl(collGoal);
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

    public int goalMode = 2;

    private class ShanXianCollGoalThread extends Thread {
        private CollGoal collGoal;

        ShanXianCollGoalThread(CollGoal collGoal) {
            this.collGoal = collGoal;
        }

        public String getMyName() {
            return "线程-" + collGoal.getName();
        }

        @Override
        public void run() {
            while (!collGoal.isOver() && isRunning) {
                if (gameStatus != GameData.STATUS_RUNNING) {
                    continue;
                }

                try {
                    Message message = new Message();
                    message.obj = collGoal;
                    mShanXianCollHandler.sendMessage(message);

                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Logger.w(TAG, "目标闪现线程中断");
                    break;
                }
            }
            this.collGoal = null;
        }
    }

    private static class ShanXianCollHandler extends Handler {
        private WeakReference<RunningParam> weakReference;
        private RunningParam mRunningParam;

        ShanXianCollHandler(RunningParam mRunningParam) {
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

            CollGoal collGoal = (CollGoal) msg.obj;
            mRunningParam.mShanXianCallBack.shanxian(collGoal);
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
