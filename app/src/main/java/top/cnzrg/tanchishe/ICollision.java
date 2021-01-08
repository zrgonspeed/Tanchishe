package top.cnzrg.tanchishe;

import android.graphics.Rect;

public interface ICollision {
    Rect getRect();

    void setRect(Rect rect);

    boolean isColl(ICollision obj);
}
