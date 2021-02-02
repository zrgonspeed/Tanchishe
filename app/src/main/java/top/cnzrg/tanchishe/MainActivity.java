package top.cnzrg.tanchishe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import top.cnzrg.tanchishe.util.Logger;
import top.cnzrg.tanchishe.util.VersionUtils;

public class MainActivity extends BaseActivity {
    private String TAG = this.getClass().getSimpleName();
    private boolean released = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.e(TAG, "onCreate()-----------------------");
    }

    @Override
    public void init() {
        Logger.e(TAG, "init()-----------------------");
        setContentView(R.layout.activity_main);

        Button bt_start = findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameSceneActivity.class));
            }
        });

        Logger.e(TAG, "版本号:" + VersionUtils.getAppVersionName(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.e(TAG, "onResume()-----------------------");
    }

    @Override
    protected void onPause() {
        Logger.e(TAG, "onResume()-----------------------");

        if (isFinishing()) {
            release();
            released = true;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Logger.e(TAG, "onDestroy()-----------------------");

        if (released) {
            super.onDestroy();
            return;
        }

        release();
        super.onDestroy();
    }

    private void release() {
        Logger.i(TAG, "释放资源");
    }
}
