package top.cnzrg.tanchishe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CollSnack implements ICollision {
    private Snack snack;
    private ImageView view;
    private Rect rect;

    public Snack getSnack() {
        return snack;
    }

    public void setSnack(Snack snack) {
        this.snack = snack;
    }

    public ImageView getView() {
        return view;
    }

    public void setView(ImageView view) {
        this.view = view;
        this.rect = new Rect((int) (view.getX()), (int) (view.getY()), (int) (view.getX() + view.getWidth()), (int) (view.getY() + view.getHeight()));
    }

    public void setX(float x) {
        view.setX(x);
        rect.left = (int) (x);
        rect.right = (int) (x + view.getWidth());
        rect.top = (int) (view.getY());
        rect.bottom = (int) (view.getY() + view.getHeight());
    }

    public void setY(float y) {
        view.setY(y);
        rect.top = (int) (y);
        rect.bottom = (int) (y + view.getHeight());
        rect.left = (int) (view.getX());
        rect.right = (int) (view.getX() + view.getWidth());
    }


    public String getName() {
        return snack.getName() + "-col";
    }

    @Override
    public Rect getRect() {
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
}
