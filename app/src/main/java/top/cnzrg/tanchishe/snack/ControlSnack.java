package top.cnzrg.tanchishe.snack;

import android.content.Context;

import static top.cnzrg.tanchishe.param.Direction.DIRECTION_DOWN;
import static top.cnzrg.tanchishe.param.Direction.DIRECTION_LEFT;
import static top.cnzrg.tanchishe.param.Direction.DIRECTION_RIGHT;
import static top.cnzrg.tanchishe.param.Direction.DIRECTION_UP;

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
        direction = DIRECTION_LEFT;
    }

    @Override
    public void turnRight() {
        direction = DIRECTION_RIGHT;
    }

    @Override
    public void turnUP() {
        direction = DIRECTION_UP;
    }

    @Override
    public void turnDown() {
        direction = DIRECTION_DOWN;
    }

    @Override
    public void destory() {

        mContext = null;
        mView = null;
    }

    public Snack getSnack() {
        return snack;
    }
}
