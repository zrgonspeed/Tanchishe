package top.cnzrg.tanchishe.goal.boom;

import java.util.ArrayList;
import java.util.List;

import top.cnzrg.tanchishe.goal.CollGoal;

public class BoomManager {
    private static BoomManager instance;
    private List<IRunningParam> params;

    public interface IRunningParam {
        void destory();
    }

    public void start(CollGoal collGoal) {
        BoomMoveGoalRunningParam param = new BoomMoveGoalRunningParam(collGoal);
        params.add(param);
        param.start();
    }

    public void destory() {
        for (IRunningParam param : params
        ) {
            param.destory();
        }

        params.clear();
        params = null;
        instance = null;
    }

    public static BoomManager getInstance() {
        if (instance == null) {
            synchronized (BoomManager.class) {
                if (instance == null) {
                    instance = new BoomManager();
                }
            }
        }

        return instance;
    }

    private BoomManager() {
        params = new ArrayList<>();
    }
}
