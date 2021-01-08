package top.cnzrg.tanchishe;

import android.widget.ImageView;

public interface IControlGoal {

    void registerGoal(Goal goal);

    void unRegisterGoal();

    void setSize(int size);

    int getSize();

    void setImage(ImageView imageView);
}
