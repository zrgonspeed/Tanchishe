package top.cnzrg.tanchishe.goal.move;

import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;

import top.cnzrg.tanchishe.RunningParam;
import top.cnzrg.tanchishe.goal.CollGoal;
import top.cnzrg.tanchishe.gamedata.Direction;
import top.cnzrg.tanchishe.gamedata.GameData;
import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.ThreadManager;

public class MoveGoalRunningParam {
    public static String TAG = "MoveGoalRunningParam";
    private static MoveGoalRunningParam instance;
    // 随机数安排
    private SecureRandom random = new SecureRandom();

    private MoveCollHandler moveCollHandler;

    private int dire = 0;
    private static int count = 0;
    // ddbug 移动目标
    private static int moveDist = 20;
    private static int moveInterval = 100;

    private MoveGoalRunningParam() {
        moveCollHandler = new MoveCollHandler(this);
    }

    public void start(CollGoal collGoal) {
        MoveGoalThread moveGoalThread = new MoveGoalThread(collGoal);
        ThreadManager.getInstance().addThread(moveGoalThread);
        moveGoalThread.start();
    }

    public void destory() {
        count = 0;
        moveCollHandler.removeCallbacksAndMessages(null);
        moveCollHandler = null;

        random = null;
        instance = null;
    }

    private class MoveGoalThread extends Thread {
        private CollGoal collGoal;

        MoveGoalThread(CollGoal collGoal) {
            this.collGoal = collGoal;
        }

        @Override
        public void run() {

            RunningParam runningParam = RunningParam.getInstance();

            while (!collGoal.isOver() && runningParam.isRunning) {
                try {
                    if (runningParam.gameStatus != GameData.STATUS_RUNNING) {
                    // 防止卡死，间隔一下
                    Thread.sleep(500);
                    continue;
                }

                dire = random.nextInt(4) + 1;
                count = random.nextInt(10) + 3;
                    for (int i = 0; i < count; i++) {
                        Message message = new Message();
                        message.obj = collGoal;
                        message.what = dire;
                        moveCollHandler.sendMessage(message);

                        Thread.sleep(moveInterval);
                    }
                } catch (InterruptedException e) {
                    Logger.w(TAG, "目标移动线程中断");
                    break;
                }
            }

            runningParam = null;
            collGoal = null;
        }
    }

    private static class MoveCollHandler extends Handler {
        private WeakReference<MoveGoalRunningParam> weakReference;
        private MoveGoalRunningParam mRunningParam;

        MoveCollHandler(MoveGoalRunningParam mRunningParam) {
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
            int dire = msg.what;

            boolean ok = moveView(collGoal.getView(), dire, moveDist);
            if (!ok) {
                // 跳出当前for循环，重新调整目标移动方向
                count = 0;
            }
        }
    }

    private static boolean moveView(ImageView goalView, int dire, int dist) {
        float x = goalView.getX();
        float y = goalView.getY();

        float newX = x;
        float newY = y;

        if (dire == Direction.DIRECTION_DOWN) {
            newY = y + dist;
        }

        if (dire == Direction.DIRECTION_UP) {
            newY = y - dist;
        }

        if (dire == Direction.DIRECTION_LEFT) {
            newX = x - dist;
        }

        if (dire == Direction.DIRECTION_RIGHT) {
            newX = x + dist;
        }

        if (newX <= 0 || newX >= GameData.SCENE_WIDTH - goalView.getWidth() || newY <= 0 || newY >= GameData.SCENE_HEIGHT - goalView.getHeight()) {
            return false;
        }

        goalView.setX(newX);
        goalView.setY(newY);
        return true;
    }

    public static MoveGoalRunningParam getInstance() {
        if (instance == null) {
            synchronized (MoveGoalRunningParam.class) {
                if (instance == null) {
                    instance = new MoveGoalRunningParam();
                }
            }
        }

        return instance;
    }
}
