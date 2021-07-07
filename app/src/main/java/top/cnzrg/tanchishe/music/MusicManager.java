package top.cnzrg.tanchishe.music;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

import top.cnzrg.tanchishe.music.lrc.LrcUtils;
import top.cnzrg.tanchishe.music.lrc.LrcView;
import top.cnzrg.tanchishe.util.Logger;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 管理音乐播放
 */
public class MusicManager {
    private static String TAG = "MusicManager";
    private static MusicManager instance;
    private MediaPlayer mediaPlayer;

    private static int STATUS_PLAYING = 1;
    private static int STATUS_PAUSE = 2;
    private static int STATUS_STOP = 3;
    private int status = STATUS_STOP;

    private static final String lrcFileName = "pfzl.lrc";
    private static final String musicFileName = "pfzl.mp3";
    private static Context context;

    private boolean isRunning = false;
    private LrcView lrcView;


    public static void setContext(Context context) {
        MusicManager.context = context;
    }

    public void play() {
        play(musicFileName, lrcFileName);
    }

    public void pause() {
        status = STATUS_PAUSE;
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void resume() {
        if (status == STATUS_PAUSE) {
            if (mediaPlayer != null) {
                status = STATUS_PLAYING;
                mediaPlayer.start();
            }
        }
    }

    public void stop() {
        status = STATUS_STOP;
        isRunning = false;

        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // 窗口移除歌词View
        if (lrcView != null) {
            removeViewFromWindow(lrcView);
            lrcView.reset();
            lrcView = null;
        }
    }

    public void hideLrc() {
        if (lrcView != null)
            lrcView.setVisibility(View.GONE);
    }

    public void showLrc() {
        if (lrcView != null)
            lrcView.setVisibility(View.VISIBLE);
    }

    private static void removeViewFromWindow(View view) {
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        wm.removeView(view);
    }

    private static void addViewToWindow(View view) {
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.gravity = Gravity.CENTER | Gravity.TOP;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }
        params.width = 600;
        params.height = 100;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;

        wm.addView(view, params);
    }

    private Handler handler = new Handler();

    // 歌词UI刷新
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "歌词runnable");
            if (status == STATUS_STOP) {
                Logger.i(TAG, "runnable STATUS_STOP");
                return;
            }

            if (status != STATUS_PLAYING) {
                Logger.d(TAG, "runnable status != STATUS_PLAYING");
                handler.postDelayed(this, 300);
                return;
            }

            if (mediaPlayer.isPlaying()) {
                Logger.d(TAG, "runnable mediaPlayer.isPlaying()");
                long time = mediaPlayer.getCurrentPosition();
                lrcView.setProgress(time, false);
            }

            handler.postDelayed(this, 300);
        }
    };

    private void play(String musicFileName, String lrcFileName) {
        isRunning = true;

        // 歌词相关
        lrcView = new LrcView(context);
        String lrcStr = LrcUtils.getLrcStrFromFile(lrcFileName);
        lrcView.loadLrc(lrcStr);

        // 将歌词view添加到窗口
        addViewToWindow(lrcView);

        // 播放音乐
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(musicFileName);
            mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Logger.i("onPrepared准备好了");
                    // 准备好了就播放
                    status = STATUS_PLAYING;
                    mediaPlayer.start();
                    handler.post(runnable);
                    //ddbug music初始进度
                    mediaPlayer.seekTo(0);
                }
            });

            // 播放完成
            mediaPlayer.setOnCompletionListener(mp -> {
                Logger.i("播放完了");
//                status = STATUS_STOP;
//                lrcView.setProgress(0, false);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            synchronized (MusicManager.class) {
                if (instance == null) {
                    instance = new MusicManager();
                }
            }
        }

        return instance;
    }
}
