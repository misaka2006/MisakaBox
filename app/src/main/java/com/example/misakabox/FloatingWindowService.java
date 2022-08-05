package com.example.misakabox;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.HashMap;


public class FloatingWindowService extends Service implements View.OnClickListener {
    public static boolean isStarted = false;
    private static final String TAG = "FloatingService";
    private static final int COL = 4;
    private static final int ROW = 10;
    private LinearLayout linearLayout;
    private LinearLayout[] RowLayouts = new LinearLayout[ROW];
    private Button[][] btn = new Button[ROW][COL];
    private Button btnClose;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View displayView;
    private SoundPool mSoundPool = null;
    private HashMap<Integer, Integer> soundID = new HashMap<Integer, Integer>();
    private String[] soundName;
    private TextView tvStop;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        //layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        //layoutParams.gravity=Gravity.CENTER;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = dip2px(this, 300);
        layoutParams.height = dip2px(this, 250);
        layoutParams.x = 800;
        layoutParams.y = 800;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.float_window, null);
            vibrator= (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            linearLayout = displayView.findViewById(R.id.ll_2);
            btnClose = displayView.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(this);
            tvStop = displayView.findViewById(R.id.tv_stop);
            tvStop.setOnClickListener(this);
            initSoundName();
            int cnt = 0;
            for (int i = 0; i < ROW; i++) {
                RowLayouts[i] = new LinearLayout(this);
                RowLayouts[i].setOrientation(LinearLayout.HORIZONTAL);

                for (int j = 0; j < COL; j++) {
                    btn[i][j] = new Button(this);
                    btn[i][j].setId(cnt++);
                    btn[i][j].setBackgroundResource(R.drawable.tv_border);
                    LinearLayout.LayoutParams weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, -2, 1);
                    btn[i][j].setLayoutParams(weight1);
                    btn[i][j].setOnClickListener(this);
                    btn[i][j].setText(soundName[cnt - 1]);
                    btn[i][j].setTextSize(10);
                    RowLayouts[i].addView(btn[i][j]);
                }
                linearLayout.addView(RowLayouts[i]);
            }
            windowManager.addView(displayView, layoutParams);

        }

        initSoundPool();
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                Log.d(TAG, "Load Completed");
            }
        });
    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        vibrator.cancel();
        vibrator.vibrate(20);
        int id = view.getId();
        if (id == btnClose.getId()) {
            isStarted=false;
            //Toast.makeText(this, "关闭", Toast.LENGTH_SHORT).show();
            windowManager.removeView(displayView);
            displayView = null;
        } else if (id == tvStop.getId()) {
            if (mSoundPool != null) {
                mSoundPool.release();
                initSoundPool();
            }
        } else {
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSoundPool.play(soundID.get(id), 1, 1, 0, 0, 1);
                }
            },500); //音频延时播放，方便操作

        }

    }


    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    public static int dip2px(Context context, float dpValue) {
        // 获取当前手机的像素密度（1个dp对应几个px）

        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f); // 四舍五入取整
    }

    /**
     * 获取图片名称获取图片的资源id的方法
     *
     * @param imageName
     * @return
     */
    public int getResource(String imageName) {
        Context ctx = getBaseContext();
        int resId = getResources().getIdentifier(imageName, "raw", ctx.getPackageName());
        return resId;
    }

    private void initSoundPool() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = null;
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mSoundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        }
        else mSoundPool=new SoundPool(2, AudioManager.STREAM_SYSTEM, 5);

        for (int i = 0; i < ROW * COL; i++) {
            soundID.put(i, mSoundPool.load(this, getResource("a" + (i + 1)), 1));
        }
    }

    private void initSoundName() {
        soundName = new String[]{"不要过来啊", "是", "黑子", "啊，那个", "哎", "抱歉", "不会让他跑掉", "吵死了", "等一等",
                "发生什么事", "（尬笑）", "给你", "给我过来", "是，给", "没什么", "嘿哈", "去吃冰激凌", "快点", "来了，是", "来了来了",
                "认真点", "你是我老妈吗！", "你在想什么啊", "风纪委员", "哦", "去死吧", "忍耐", "什么嘛", "什么什么", "太感谢了",
                "为什么", "我是御坂美琴", "我说过了", "洗澡歌", "不要过来！", "找到了", "这个是我的", "真的可以吗", "真是的", "正确"};
    }
}
