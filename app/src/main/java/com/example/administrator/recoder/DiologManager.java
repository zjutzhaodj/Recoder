package com.example.administrator.recoder;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Zhaodj on 2015/11/30.
 */
public class DiologManager {


    private Dialog mDialog;
    private ImageView mIcon;
    private ImageView mVoice;
    private TextView mLabel;
    private Context mContext;

    public DiologManager(Context mContext) {
        this.mContext = mContext;
    }


    /**
     * 初始化dialog，并显示
     */
    public void showRecordingDialog(){
        mDialog=new Dialog(mContext,R.style.ThemeAudioDialog);
        LayoutInflater layoutInflater=LayoutInflater.from(mContext);
        View view=layoutInflater.inflate(R.layout.dialog_recoder,null);
        mDialog.setContentView(view);
        mIcon= (ImageView) mDialog.findViewById(R.id.id_recoder_diolog_icon);
        mVoice= (ImageView) mDialog.findViewById(R.id.id_recoder_dialog_voice);
        mLabel= (TextView) mDialog.findViewById(R.id.id_text);
        mDialog.show();
    }


    /**
     * 语音录制过程中显示的dialog
     */
    public void recording(){
        if(mDialog!=null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.recorder);
            mLabel.setText("手指上滑，取消发送");
        }
    }

    /**
     * 手势滑动想要取消时候弹出的dialog
     */

    public void wantTocCancel(){
        if(mDialog!=null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.cancel);
            mLabel.setText("松开，取消发送");

        }

    }

    /**
     * 语音时间太短的dialog
     */

    public void tooShort(){
        if(mDialog!=null && mDialog.isShowing()){
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);
            mIcon.setImageResource(R.drawable.voice_to_short);
            mLabel.setText("录音时间过短");

        }

    }

    /**
     * 取消dialog
     */

    public void dismissDialog(){
        if(mDialog!=null && mDialog.isShowing()){
           mDialog.dismiss();
            mDialog=null;


        }
    }


    /**
     * 根据传入的level，来改变音量大小的图标显示
     * @param level
     */

    public void updateVoiceLevel(int level){
        if(mDialog!=null && mDialog.isShowing()){
            //获取对应的资源id
            int resid=mContext.getResources().getIdentifier("v"+level,"drawable",mContext.getPackageName());
            mVoice.setImageResource(resid);

        }

    }
}
