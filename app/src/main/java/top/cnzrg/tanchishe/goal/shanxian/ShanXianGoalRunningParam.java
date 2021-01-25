package top.cnzrg.tanchishe.goal.shanxian;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import top.cnzrg.tanchishe.RunningParam;
import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;

public class ShanXianGoalRunningParam {
    public static String TAG = "ShanXianGoalRunningParam";
    private static ShanXianGoalRunningParam instance;

    private InnerHandler handler;

    // 生成回调
    private CreateShanXianCallBack mCreateShanXianCallBack;

    public interface CreateShanXianCallBack {
        void shanxian(CollGoal collGoal);
    }

    public void setCreateShanXianCallBack(CreateShanXianCallBack mCreateShanXianCallBack) {
        this.mCreateShanXianCallBack = mCreateShanXianCallBack;
    }

    public void start(CreateShanXianCallBack callBack, CollGoal collGoal) {
        setCreateShanXianCallBack(callBack);

        ShanXianGoalRefreshThread thread = new ShanXianGoalRefreshThread(collGoal);
        ThreadManager.getInstance().addThread(thread);
        thread.start();
    }

    public void destory() {
        handler.removeCallbacksAndMessages(null);
        handler = null;

        instance = null;
    }

    /**
     * 生成线程
     */
    private class ShanXianGoalRefreshThread extends Thread {
        private CollGoal collGoal;

        ShanXianGoalRefreshThread(CollGoal collGoal) {
            this.collGoal = collGoal;
        }

        public String getMyName() {
            return "线程-" + collGoal.getName();
        }

        @Override
        public void run() {
            try {
                RunningParam runningParam = RunningParam.getInstance();

                while (!collGoal.isOver() && runningParam != null && runningParam.isRunning) {
                    if (runningParam.gameStatus != GameData.STATUS_RUNNING) {
                        continue;
                    }

                    Message message = new Message();
                    message.obj = collGoal;
                    handler.sendMessage(message);

                    Thread.sleep(3000);

                }
                this.collGoal = null;
            } catch (InterruptedException e) {
                Logger.w(TAG, "目标闪现线程中断");
            }
        }
    }

    private static class InnerHandler extends Handler {
        private WeakReference<ShanXianGoalRunningParam> weakReference;
        private ShanXianGoalRunningParam param;

        InnerHandler(ShanXianGoalRunningParam param) {
            if (param != null) {
                weakReference = new WeakReference<>(param);
            }
        }

        @Override
        synchronized public void handleMessage(Message msg) {
            param = weakReference.get();
            if (param == null) {
                return;
            }

            CollGoal collGoal = (CollGoal) msg.obj;
            param.mCreateShanXianCallBack.shanxian(collGoal);
        }
    }

    public static ShanXianGoalRunningParam getInstance() {
        if (instance == null) {
            synchronized (ShanXianGoalRunningParam.class) {
                if (instance == null) {
                    instance = new ShanXianGoalRunningParam();
                }
            }
        }
        return instance;
    }

    private ShanXianGoalRunningParam() {
        handler = new InnerHandler(this);
    }

}
