package com.example.administrator.recoder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


/**
 * Created by Zhaodj on 2015/11/30.
 */
public class AudioRecoderButton extends Button implements AudioManager.AudioStateListener {


    /**
     * 几种点击的状态
     * 1. 正常状态
     * 2. 录制状态
     * 3. 取消状态（手势滑出规定范围）
     */
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCLE = 3;
    //是否触发longclick
    private boolean mReady;

    //当前的状态
    private int mCurState = STATE_NORMAL;
    //是否正在录制
    private boolean isRecording;
    //自己规定的向上滑出多少距离 判断为取消
    private static final int DISTANCE_Y_CANCEL = 50;
    //弹出框初始化
    private DiologManager mdiologManager;
    //音频管理初始化
    private AudioManager mAudioManager;


    public AudioRecoderButton(Context context) {
        this(context, null);
    }

    /**
     * 构造函数，初始化相关数据，设置监听
     *
     * @param context
     * @param attrs
     */
    public AudioRecoderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mdiologManager = new DiologManager(getContext());
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("tag","long click");
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
        //获取外部存储的路径
        String dir = Environment.getExternalStorageDirectory() + "/recoder_audios";
        //初始化音频关联类
        mAudioManager = AudioManager.getmInstance(dir);
        mAudioManager.setOnAudioStateListener(this);
    }

    /**
     * 定义一个录制结束的回调接口
     */

    public interface AudioFinishRecorderListener {
        void onfinish(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener mListener) {
        this.mListener = mListener;
    }


    /**
     * 当prepare结束后的回调函数，向handler发送一个prepare完成的信息
     */
    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 定义一个runnable对象
     * 每隔0.1秒，去获取一次来获取音量高低，向handler发送一个音量变化的信息
     * 将recording的时间累积到mtime中
     */
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mtime += 0.1f;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    /**
     * handler所要接收的信息
     * mtime 点击的持续时间
     */

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_DIALOG_DISMISS = 0X112;
    private float mtime;


    /**
     * handler根据发送来的信息进行相应的处理
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    //显示正在录制的dialog
                    mdiologManager.showRecordingDialog();
                    isRecording = true;
                    //启动获取音量变化的线程
                    new Thread(mGetVoiceLevelRunnable).start();

                    break;
                case MSG_VOICE_CHANGED:

                    mdiologManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));

                    break;
                case MSG_DIALOG_DISMISS:
                    mdiologManager.dismissDialog();

                    break;

            }
        }
    };

    /**
     * 重写ontouchevent事件
     *
     * @param event
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d("tag", " down " );
                changeState(STATE_RECORDING);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d("tag", " move" );

                if (isRecording) {
                    if (wantToCancle(x, y)) {
                        changeState(STATE_WANT_TO_CANCLE);
                    } else {
                        changeState(STATE_RECORDING);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                Log.d("tag", " up " );
                //如果没有初始化完毕
                if (!mReady) {
                    Log.d("tag", 1 + ":  " + mtime + "");
                    reset();
                    return super.onTouchEvent(event);
                }
                //如果点击的时间<0.6s，显示说话时间太短的dialog，并释放掉相应的资源
                if (!isRecording || mtime < 0.6f) {
                    Log.d("tag", 2 + ":  " + mtime + "");
                    isRecording = false;
                    mdiologManager.tooShort();
                    mAudioManager.cancle();
                    mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1300);

                    //正常情况下UP操作
                } else if (mCurState == STATE_RECORDING) {
                    Log.d("tag", 3 + ":  " + mtime + "");
                    mdiologManager.dismissDialog();
                    mAudioManager.release();
                    if (mListener != null) {
                        mListener.onfinish(mtime, mAudioManager.getCurrentFilepath());
                    }

                }
                //用户想要取消时的UP 操作
                else if (mCurState == STATE_WANT_TO_CANCLE) {
                    mAudioManager.release();
                    mdiologManager.dismissDialog();
                }
                reset();

                break;
        }
        return super.onTouchEvent(event);
    }

    // 将所有数据设置为初始化的状态
    private void reset() {

        isRecording = false;
        changeState(STATE_NORMAL);
        mReady = false;
        mtime = 0;
    }

    /**
     * 根据传入的相对坐标（x，y）来判断是否用户想要取消
     * 当水平方向滑出button的范围或者垂直方向滑出了 自己定义的DISTANCE_Y_CANCEL，则判断为用户想要取消
     * @param x
     * @param y
     * @return
     */

    private boolean wantToCancle(int x, int y) {
        if (x < 0 || x > getWidth()) {
            return true;
        }
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }


    /**
     * 根据传入的状态，完成对应状态的显示
     *
     * @param stateRecording 传入的状态
     */

    private void changeState(int stateRecording) {

        //如果状态跟当前的状态一致，则不不做修改
        if (mCurState != stateRecording) {
            switch (stateRecording) {
                case STATE_NORMAL:
                    setText("按住 说话");
                    break;
                case STATE_RECORDING:
                    setText("松开 结束");
                    //显示对应的dialog
                    if (isRecording) {
                        mdiologManager.recording();
                    }
                    break;

                case STATE_WANT_TO_CANCLE:

                    setText("松开手指 取消发送");
                    mdiologManager.wantTocCancel();
                    break;
            }
        }
        mCurState = stateRecording;
    }


}
