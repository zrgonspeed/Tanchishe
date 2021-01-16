package top.cnzrg.tanchishe.goal;

import android.content.Context;
import android.widget.ImageView;

public class ControlGoal implements IControlGoal {
    private Goal goal;
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
        this.goal = goal;
    }

    @Override
    public void unRegisterGoal() {
        this.goal = null;
    }

    @Override
    public void setSize(int size) {
        goal.setSize(size);
    }

    @Override
    public int getSize() {
        return goal.getSize();
    }

    @Override
    public void setImage(ImageView imageView) {

    }

    @Override
    public void destory() {
        mContext = null;
        mView = null;
    }

    public Goal getGoal() {
        return goal;
    }
}
