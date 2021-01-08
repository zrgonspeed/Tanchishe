package top.cnzrg.tanchishe;

import android.content.Context;

import static top.cnzrg.tanchishe.Direction.DIRECTION_DOWN;
import static top.cnzrg.tanchishe.Direction.DIRECTION_LEFT;
import static top.cnzrg.tanchishe.Direction.DIRECTION_RIGHT;
import static top.cnzrg.tanchishe.Direction.DIRECTION_UP;

public class ControlSnack implements IControlSnack {
    private Snack snack;
    private Context mContext;
    private IControlSnackView mView;
    private static ControlSnack instance;

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    private int direction = 1;

    private ControlSnack() {
    }

    public static ControlSnack getInstance() {
        if (instance == null) {
            synchronized (ControlSnack.class) {
                if (instance == null) {
                    instance = new ControlSnack();
                }
            }
        }

        return instance;
    }

    public void setView(IControlSnackView view) {
        this.mView = view;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    @Override
    public void registerSnack(Snack snack) {
        this.snack = snack;
    }

    @Override
    public void unRegisterSnack() {
        this.snack = null;
    }

    @Override
    public void turnLeft() {
        System.out.println("左");
        direction = DIRECTION_LEFT;
    }

    @Override
    public void turnRight() {
        System.out.println("右");
        direction = DIRECTION_RIGHT;
    }

    @Override
    public void turnUP() {
        System.out.println("上");
        direction = DIRECTION_UP;
    }

    @Override
    public void turnDown() {
        System.out.println("下");
        direction = DIRECTION_DOWN;
    }

}
