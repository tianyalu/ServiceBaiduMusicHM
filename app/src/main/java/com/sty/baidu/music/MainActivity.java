package com.sty.baidu.music;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sty.baidu.music.service.IService;
import com.sty.baidu.music.service.MusicService;

/**
 *  混合方式开启服务[既保证服务长期在后台运行又可以调用服务中的方法]:
 *      1.先调用startService方法以保证服务在后台长期运行
 *      2.调用bindService 以获取我们定义的中间人对象，从而调用服务中的方法
 *      3.调用unbindService 这时候服务不会被销毁
 *      4.调用stopService 停止服务
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnPlay;
    private Button btnStop;
    private Button btnReplay;

    private IService iService; //定义的中间人对象
    private MyConn conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();

        //0.调用startService方法开启服务以保证服务在后台长期运行
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);

        //1.调用bindService 目的是为了获取定义的中间人对象
        conn = new MyConn();
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        //在Activity销毁的时候取消绑定服务
        unbindService(conn);
        super.onDestroy();
    }

    private void initViews(){
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnReplay = (Button) findViewById(R.id.btn_replay);
    }

    private void setListeners(){
        btnPlay.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnReplay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                iService.callPlayMusic(); //调用播放音乐的方法
                break;
            case R.id.btn_stop:
                iService.callStopMusic(); //调用暂停播放音乐的方法
                break;
            case R.id.btn_replay:
                iService.callReplayMusic(); //调用继续播放音乐的方法
                break;
            default:
                break;
        }
    }

    private class MyConn implements ServiceConnection{

        //当连接成功时调用
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获取定义的中间人对象
            iService = (IService) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
