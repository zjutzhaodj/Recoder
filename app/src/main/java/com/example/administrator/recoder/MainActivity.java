package com.example.administrator.recoder;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private ListView mlistview;
    private ArrayAdapter<Recorder> mAdapter;
    private List<Recorder> mDatas=new ArrayList<Recorder>();
    private AudioRecoderButton mAudioRecorderButton;
    private View mAniView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mlistview= (ListView) findViewById(R.id.listview);
        mAudioRecorderButton= (AudioRecoderButton) findViewById(R.id.bt);
        mAudioRecorderButton.setAudioFinishRecorderListener(new AudioRecoderButton.AudioFinishRecorderListener() {
            @Override
            public void onfinish(float seconds, String filePath) {
                Recorder recorder=new Recorder(seconds,filePath);
                mDatas.add(recorder);
                mAdapter.notifyDataSetChanged();
                mlistview.setSelection(mDatas.size()-1);

            }
        });
        mAdapter=new RecorderAdapter(this,mDatas);
        mlistview.setAdapter(mAdapter);

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if(mAniView!=null){
                    mAniView.setBackgroundResource(R.drawable.adj);
                    mAniView=null;
                }
                //播放动画
                mAniView=view.findViewById(R.id.recoder_anim);
                mAniView.setBackgroundResource(R.drawable.play_anim);
                final AnimationDrawable anim= (AnimationDrawable) mAniView.getBackground();
                anim.start();
                //播放音频
                MediaManager.playSound(mDatas.get(position).fielPath, new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d("tag2","over:"+position);
                        anim.stop();
                        mAniView.setBackgroundResource(R.drawable.adj);
                    }
                });
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
    }

    class Recorder{

        float time;
        String fielPath;

        public Recorder(float time, String fielPath) {
            this.time = time;
            this.fielPath = fielPath;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }

        public void setFielPath(String fielPath) {
            this.fielPath = fielPath;
        }

        public String getFielPath() {
            return fielPath;
        }
    }
}
