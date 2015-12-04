package com.example.administrator.recoder;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2015/11/30.
 */
public class RecorderAdapter extends ArrayAdapter<MainActivity.Recorder> {

    private List<MainActivity.Recorder> mDatas;
    private Context context;
    private int mMinItemWidth;
    private int mMaxItemWidth;
    private LayoutInflater layoutInflater;


    public RecorderAdapter(Context context, List<MainActivity.Recorder> mDatas ) {
        super(context, -1, mDatas);
        this.mDatas = mDatas;
        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        mMaxItemWidth= (int) (outMetrics.widthPixels*0.7f);
        mMinItemWidth= (int) (outMetrics.widthPixels*0.15f);
        layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.item_recorder,parent,false);
            holder=new ViewHolder();
            holder.seconds= (TextView) convertView.findViewById(R.id.id_recorder_time);
            holder.length=convertView.findViewById(R.id.id_recorder_length);
            convertView.setTag(holder);

        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.seconds.setText(Math.round(getItem(position).time) + "\"");
        ViewGroup.LayoutParams lp=holder.length.getLayoutParams();

        Log.d("tag1","minwidth: "+mMinItemWidth);
        Log.d("tag1","maxwidth: "+mMaxItemWidth);


        lp.width= (int) (mMinItemWidth+((mMaxItemWidth/30f) *getItem(position).time));
        Log.d("tag1","getItem(position).time: "+getItem(position).time);
        Log.d("tag1","lp.width: "+lp.width);

        return convertView;
    }

    class ViewHolder{
        TextView seconds;
        View length;
    }
}
