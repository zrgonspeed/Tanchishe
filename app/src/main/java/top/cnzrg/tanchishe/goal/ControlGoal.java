package top.cnzrg.tanchishe.goal;

import android.content.Context;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import top.cnzrg.tanchishe.param.GameData;

public class ControlGoal implements IControlGoal {
    private Context mContext;
    private IControlGoalView mView;
    private static ControlGoal instance;

    private ControlGoal() {
    }

    public void setView(IControlGoalView view) {
        this.mView = view;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public static ControlGoal getInstance() {
        if (instance == null) {
            synchronized (ControlGoal.class) {
                if (instance == null) {
                    instance = new ControlGoal();
                }
            }
        }

        return instance;
    }

    @Override
    public void registerGoal(Goal goal) {
    }

    @Override
    public void unRegisterGoal() {
    }

    @Override
    public void setSize(int size) {

    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void setImage(ImageView imageView) {

    }

    @Override
    public void destory() {
        mContext = null;
        mView = null;

        collGoals.clear();
        collGoals = null;

        instance = null;
    }

    private List<CollGoal> collGoals = new ArrayList<>();

    public CollGoal getCollGoal(int i) {
        return collGoals.get(i);
    }

    public void addCollGoal(CollGoal collGoal) {
        collGoals.add(collGoal);
    }

    public CollGoal newCollGoal(ImageView view) {
        CollGoal collGoal = new CollGoal();
        collGoal.setGoal(createGoal());
        collGoal.setView(view);

        collGoals.add(collGoal);
        return collGoal;
    }

    private Goal createGoal() {
        Goal goal = new Goal();
        goal.setName("目标-" + ++GameData.GOAL_COUNT);
        return goal;
    }

    public List<CollGoal> getCollGoals() {
        return collGoals;
    }
}
