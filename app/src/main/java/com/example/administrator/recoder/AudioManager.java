package com.example.administrator.recoder;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 单例模式下的音频管理类
 *
 * Created by Zhaodj on 2015/11/30.
 */
public class AudioManager {

    /**
     * mDir 目录
     * mCurrentFilepath 文件路径
     *isprepare 是否准备完毕
     */
    private MediaRecorder mMediaRecoder;
    private String mDir;
    private String mCurrentFilepath;

    private static AudioManager mInstance;

    private boolean isprepare;

    private AudioManager(String dir) {
        mDir=dir;
    }

    public static AudioManager getmInstance(String dir) {

        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(dir);
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化音频
     *
     */

    public void prepareAudio() {
        try {
            isprepare=false;
            File dir=new File(mDir);
            if(!dir.exists())
                dir.mkdir();

            String fileName=generateFileName();
            //创建相应目录下的文件
            File file=new File(dir,fileName);
            //初
            //
            // 始化当前文件路径
            mCurrentFilepath=file.getAbsolutePath();
            mMediaRecoder=new MediaRecorder();
            mMediaRecoder.setOutputFile(file.getAbsolutePath());
            //设置Mediarecorder的音频源为麦克风
            mMediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频的格式
            mMediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            //设置音频的编码
            mMediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mMediaRecoder.prepare();
            mMediaRecoder.start();
            isprepare=true;
            if(mListener!=null){
                mListener.wellPrepared();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机产生一个音频文件的文件名
     * @return
     */
    private String generateFileName() {

        return UUID.randomUUID().toString()+".amr";
    }

    /**
     *
     * @param maxlevel 传入的最大音量level，可以根据自己的需求来设置
     *                 mMediaRecoder.getMaxAmplitude() 1-32767
     *                 mMediaRecoder.getMaxAmplitude() /32768  -> 0-1
     *                 mMediaRecoder.getMaxAmplitude() /32768 *maxlevel ->大于0，小于maxlevel 娶不到 maxlevel
     *                 +1以后，就可以取到 1->maxlevel
     * @return
     */
    public int getVoiceLevel(int maxlevel) {
        if(isprepare){
            try {
                //maxlevel*mMediaRecoder.getMaxAmplitude() 1-32767
                return maxlevel*mMediaRecoder.getMaxAmplitude()/32768+1;
            } catch (Exception e) {
            }
        }
        return 1;
    }

    /**
     * 释放 mMediaRecoder
     */
    public void release() {
        mMediaRecoder.stop();
        mMediaRecoder.release();
        mMediaRecoder=null;

    }

    /**
     * 释放 mMediaRecoder
     * 并且将相应的音频文件删除
     */

    public void cancle() {
        release();
        if(mCurrentFilepath!=null){
            File file=new File(mCurrentFilepath);
            file.delete();
            mCurrentFilepath=null;
        }
    }

    public String getCurrentFilepath() {
        return mCurrentFilepath;
    }

    public interface AudioStateListener {
        void wellPrepared();

    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener mListener) {
        this.mListener = mListener;
    }


}
