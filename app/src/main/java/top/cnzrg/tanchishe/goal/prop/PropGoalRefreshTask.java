package top.cnzrg.tanchishe.goal.prop;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.List;

import top.cnzrg.tanchishe.RunningParam;
import top.cnzrg.tanchishe.goal.ControlGoal;
import top.cnzrg.tanchishe.param.GameData;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;

public class PropGoalRefreshTask {
    public static String TAG = "PropGoalRefreshTask";
    private static PropGoalRefreshTask instance;

    // 几分之几的几率生成道具，间隔1秒
    private int a = 4;

    // 随机数安排
    private SecureRandom random = new SecureRandom();

    private InnerHandler handler;

    // 道具生成回调
    private CreatePropCallBack mCreatePropCallBack;

    public interface CreatePropCallBack {
        void createPropCollGoal();
    }

    public void setCreatePropCallBack(CreatePropCallBack mCreatePropCallBack) {
        this.mCreatePropCallBack = mCreatePropCallBack;
    }

    public void start(CreatePropCallBack callBack) {
        setCreatePropCallBack(callBack);

        PropGoalRefreshThread thread = new PropGoalRefreshThread();
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
     * 道具生成线程
     */
    private class PropGoalRefreshThread extends Thread {
        @Override
        public void run() {
            try {
                RunningParam runningParam = RunningParam.getInstance();

                while (runningParam != null && runningParam.isRunning) {
                    if (runningParam.gameStatus != GameData.STATUS_RUNNING) {
                        continue;
                    }

                    List<PropCollGoal> collPropGoals = ControlGoal.getInstance().getPropCollGoals();

                    if (collPropGoals.size() == 0) {
                        // 4分之1几率生成道具
                        if (random.nextInt(a) == 3) {
                           handler.sendEmptyMessage(0);
                        }
                    }

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Logger.w(TAG, "道具生成线程中断");
            }
        }
    }

    private static class InnerHandler extends Handler {
        private WeakReference<PropGoalRefreshTask> weakReference;
        private PropGoalRefreshTask task;

        InnerHandler(PropGoalRefreshTask task) {
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

            task.mCreatePropCallBack.createPropCollGoal();
        }
    }

    public static PropGoalRefreshTask getInstance() {
        if (instance == null) {
            synchronized (PropGoalRefreshTask.class) {
                if (instance == null) {
                    instance = new PropGoalRefreshTask();
                }
            }
        }
        return instance;
    }

    private PropGoalRefreshTask() {
        handler = new InnerHandler(this);
    }

}
