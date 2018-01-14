package com.sty.baidu.music;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;
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
    private static SeekBar sbSeekBar;
    private Button btnPlayNetMusic;
    private Button btnStopPlayNetMusic;

    private IService iService; //定义的中间人对象
    private MyConn conn;
    private static String dataSourceNetPath = "http://192.168.1.8/newsServiceHM/media/bugua.mp3";   //播放网络音乐

    public static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //(1)获取msg携带的数据
            Bundle data = msg.getData();
            //(2)获取当前进度和总进度
            int duration = data.getInt("duration");
            int currentPosition = data.getInt("currentPosition");
            //(3)设置seekBar的最大进度和当前进度
            sbSeekBar.setMax(duration);
            sbSeekBar.setProgress(currentPosition);
        }
    };

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
        sbSeekBar = (SeekBar) findViewById(R.id.sb_seek_bar);
        btnPlayNetMusic = findViewById(R.id.btn_play_net_music);
        btnStopPlayNetMusic = findViewById(R.id.btn_stop_play_net_music);
    }

    private void setListeners(){
        btnPlay.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnReplay.setOnClickListener(this);
        btnPlayNetMusic.setOnClickListener(this);
        btnStopPlayNetMusic.setOnClickListener(this);

        //2.给seekBar设置监听
        sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            //开始拖动时执行
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //当停止拖动时执行
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //设置播放位置
                iService.callSeekToPosition(seekBar.getProgress());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                playLocalMusicRequestPermission(); //调用播放音乐的方法(没有权限时动态申请)
                break;
            case R.id.btn_stop:
                iService.callStopMusic(); //调用暂停播放音乐的方法
                break;
            case R.id.btn_replay:
                iService.callReplayMusic(); //调用继续播放音乐的方法
                break;
            case R.id.btn_play_net_music:
                playNetMusic();  //调用播放网络音乐的方法
                break;
            case R.id.btn_stop_play_net_music:
                stopPlayNetMusic();  //调用停止播放网络音乐的方法
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

    private void playLocalMusicRequestPermission(){
        if(PermissionsUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            iService.callPlayMusic(); //调用播放音乐的方法
        }else{
            PermissionsUtil.requestPermission(this, new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permission) {
                    iService.callPlayMusic();
                }

                @Override
                public void permissionDenied(@NonNull String[] permission) {
                    Toast.makeText(MainActivity.this, "您拒绝了外置存储的访问权限", Toast.LENGTH_LONG).show();
                }
            }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    private MediaPlayer player;
    /**
     * 点击按钮播放网络音乐
     */
    private void playNetMusic(){
        //1.初始化mediaPlayer
        if(player == null) {
            player = new MediaPlayer();
        }
        //2.设置要播放的资源path 可以是本地的，也可以是网络的
        try{
            //3.重置播放器，避免非法状态引起的bug
            player.reset();
            player.setDataSource(dataSourceNetPath);

            //4.1准备播放(同步方式)
            //player.prepare();
            //5.1开始播放
            //player.start();

            //4.2准备播放(异步方式)
            player.prepareAsync();
            //设置一个准备完成的监听
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //5.2开始播放
                    player.start();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 点击按钮停止播放网络音乐
     */
    private void stopPlayNetMusic(){
        if(player != null){
            player.stop();
        }
    }
}
