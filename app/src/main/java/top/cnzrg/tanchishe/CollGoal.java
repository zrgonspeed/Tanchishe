package top.cnzrg.tanchishe;

import android.graphics.Rect;
import android.widget.ImageView;

public class CollGoal implements ICollision {
    private ImageView view;
    private Goal goal;
    private Rect rect;

    public void setView(ImageView view) {
        this.view = view;
        this.rect = new Rect((int) (view.getX()), (int) (view.getY()), (int) (view.getX() + view.getWidth()), (int) (view.getY() + view.getHeight()));

    }

    public ImageView getView() {
        return view;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Goal getGoal() {
        return goal;
    }

    @Override
    public Rect getRect() {
        rect.set((int) (view.getX()), (int) (view.getY()), (int) (view.getX() + view.getWidth()), (int) (view.getY() + view.getHeight()));
        return rect;
    }

    @Override
    public void setRect(Rect rect) {

    }

    @Override
    synchronized public boolean isColl(ICollision obj) {
        if (getRect().intersect(obj.getRect())) {
            return true;
        }
        return false;
    }

    public String getName() {
        return goal.getName() + "-col";
    }
}
