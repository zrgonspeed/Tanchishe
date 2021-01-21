package top.cnzrg.tanchishe.snack;

import android.graphics.Rect;
import android.widget.ImageView;

import top.cnzrg.tanchishe.ICollision;

public class CollSnack implements ICollision {
    private int curProps = 0;

    public float lastX;
    public float lastY;
    private Snack snack;
    private ImageView view;
    private Rect rect;

    public int getCurProps() {
        return curProps;
    }

    public void setCurProps(int curProps) {
        this.curProps = curProps;
    }

    public Snack getSnack() {
        return snack;
    }

    public void setSnack(Snack snack) {
        this.snack = snack;
    }

    public ImageView getView() {
        return view;
    }

    public float getLastX() {
        return lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setView(ImageView view) {
        this.view = view;
        this.rect = new Rect((int) (view.getX()), (int) (view.getY()), (int) (view.getX() + view.getWidth()), (int) (view.getY() + view.getHeight()));
        lastX = view.getX();
        lastY = view.getY();
    }

    public void setXY(float x, float y) {
        lastX = view.getX();
        lastY = view.getY();

        view.setX(x);
        view.setY(y);

        rect.left = (int) (x);
        rect.right = (int) (x + view.getWidth());
        rect.top = (int) (view.getY());
        rect.bottom = (int) (view.getY() + view.getHeight());

        if (next == null) {
            return;
        }

        next.setXY(lastX, lastY);

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

    private CollSnack next;

    public void addBody(CollSnack collSnack) {
        this.next = collSnack;
        snack.setLen(snack.getLen() + 1);
    }

    public CollSnack nextBody() {
        return this.next;
    }
}
