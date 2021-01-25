package top.cnzrg.tanchishe.goal.boom;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.List;

import top.cnzrg.tanchishe.RunningParam;
import top.cnzrg.tanchishe.goal.ControlGoal;
import top.cnzrg.tanchishe.goal.prop.PropCollGoal;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;

public class BoomGoalRefreshTask {
    public static String TAG = "BoomGoalRefreshTask";
    private static BoomGoalRefreshTask instance;

    // 几分之几的几率生成，间隔1秒
    private int a = 4;

    // 随机数安排
    private SecureRandom random = new SecureRandom();

    private InnerHandler handler;

    // 生成回调
    private CreateBoomCallBack mCreateBoomCallBack;

    public interface CreateBoomCallBack {
        void createBoomCollGoal();
    }

    public void setCreateBoomCallBack(CreateBoomCallBack mCreateBoomCallBack) {
        this.mCreateBoomCallBack = mCreateBoomCallBack;
    }

    public void start(CreateBoomCallBack callBack) {
        setCreateBoomCallBack(callBack);

        BoomGoalRefreshThread thread = new BoomGoalRefreshThread();
        ThreadManager.getInstance().addThread(thread);
        thread.start();
    }

    public void destory() {
        handler.removeCallbacksAndMessages(null);
        handler = null;

        random = null;
        instance = null;
    }

    /**
     * 炸弹生成线程
     */
    private class BoomGoalRefreshThread extends Thread {
        @Override
        public void run() {
            try {
                RunningParam runningParam = RunningParam.getInstance();

                while (runningParam != null && runningParam.isRunning) {
                    if (runningParam.gameStatus != GameData.STATUS_RUNNING) {
                        continue;
                    }

                    List<BoomCollGoal> collPropGoals = ControlGoal.getInstance().getBoomCollGoals();

                    if (collPropGoals.size() == 0) {
                        // 几率生成
                        if (random.nextInt(a) == 3) {
                           handler.sendEmptyMessage(0);
                        }
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Logger.w(TAG, "炸弹生成线程中断");
            }
        }
    }

    private static class InnerHandler extends Handler {
        private WeakReference<BoomGoalRefreshTask> weakReference;
        private BoomGoalRefreshTask task;

        InnerHandler(BoomGoalRefreshTask task) {
            if (task != null) {
                weakReference = new WeakReference<>(task);
            }
        }

        @Override
        synchronized public void handleMessage(Message msg) {
            task = weakReference.get();
            if (task == null) {
                return;
            }

            task.mCreateBoomCallBack.createBoomCollGoal();
        }
    }

    public static BoomGoalRefreshTask getInstance() {
        if (instance == null) {
            synchronized (BoomGoalRefreshTask.class) {
                if (instance == null) {
                    instance = new BoomGoalRefreshTask();
                }
            }
        }
        return instance;
    }

    private BoomGoalRefreshTask() {
        handler = new InnerHandler(this);
    }

}
