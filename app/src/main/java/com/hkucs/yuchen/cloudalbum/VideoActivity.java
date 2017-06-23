package com.hkucs.yuchen.cloudalbum;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import java.util.ArrayList;


public class VideoActivity extends AppCompatActivity {

    private VideoView videoView ;
    private Button btn_previous;
    private Button btn_next;
    private TextView textView;
    private ArrayList<String> videoList;
    String address = "http://i.cs.hku.hk/~htlin/php/videos/";
    String videoName;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        Intent intent =getIntent();
        videoList = intent.getStringArrayListExtra("arrayList");


        //本地的视频 需要在手机SD卡根目录添加一个 fl1234.mp4 视频
        String videoUrl1 = Environment.getExternalStorageDirectory().getPath()+"/fl1234.mp4" ;

        //网络视频

        videoName = intent.getStringExtra("videoName");
        uri = Uri.parse(address+videoName);

        videoView = (VideoView)this.findViewById(R.id.videoView );

        //设置视频控制器
        videoView.setMediaController(new MediaController(this));

        //播放完成回调
        videoView.setOnCompletionListener( new MyPlayerOnCompletionListener());

        //设置视频路径
        videoView.setVideoURI(uri);

        //开始播放视频
        videoView.start();

        textView = (TextView) findViewById(R.id.video_Name);
        textView.setText(videoName);

        btn_previous = (Button) findViewById(R.id.btn_previous);
        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;
                for (int i=0;i<videoList.size();i++){
                    if (videoList.get(i).equals(videoName)){
                        index = i;
                    }
                }
                if (index==0){
                    index = videoList.size()-1;
                    videoName = videoList.get(index);
                }else{
                    index--;
                    videoName = videoList.get(index);
                }
                textView.setText(videoName);
                uri = Uri.parse(address+videoName);
                videoView.setVideoURI(uri);
                videoView.start();
            }
        });
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = 0;
                for (int i=0;i<videoList.size();i++){
                    if (videoList.get(i).equals(videoName)){
                        index = i;
                    }
                }
                if (index==videoList.size()-1){
                    index = 0;
                    videoName = videoList.get(index);
                }else{
                    index++;
                    videoName = videoList.get(index);
                }
                textView.setText(videoName);
                uri = Uri.parse(address+videoName);
                videoView.setVideoURI(uri);
                videoView.start();
            }
        });

    }

    class MyPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            Toast.makeText( VideoActivity.this, "Finish", Toast.LENGTH_SHORT).show();
        }
    }



}
