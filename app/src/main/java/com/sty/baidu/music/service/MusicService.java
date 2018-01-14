package com.sty.baidu.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sty.baidu.music.MainActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 音乐播放的服务
 * Created by Steven.T on 2017/12/13/0013.
 */

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    private String dataSourcePath;
    private String dataSourceNetPath;

    //2.把定义的中间人对象返回
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    //服务第一次开启时调用
    @Override
    public void onCreate() {
        //[1]初始化MediaPlayer
        mediaPlayer = new MediaPlayer();
        dataSourceNetPath = "http://192.168.1.8/newsServiceHM/media/bugua.mp3";   //播放网络音乐
        dataSourcePath = Environment.getExternalStorageDirectory().getPath() + File.separator
                + "sty" + File.separator + "xpg.mp3";
        Log.i("Tag", "dataSourcePath:" + dataSourcePath);
        super.onCreate();
    }

    //当服务销毁的时候调用
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //设置播放音乐到指定位置的方法
    public void seekToPosition(int position){
        mediaPlayer.seekTo(position);
    }

    //音乐播放了
    public void playMusic(){
        Log.i("Tag", "音乐播放了");
        try{
            //[1.1]重置播放器，避免非法状态引起的bug
            mediaPlayer.reset();
            //[2]设置用来播放的资源path,可以是本地的也可以是网络路径
            mediaPlayer.setDataSource(dataSourcePath);
            //[3]准备播放音乐
            mediaPlayer.prepare();
            //[4]开始播放
            mediaPlayer.start();
            //[5]更新进度条
            updateSeekBar();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //更新进度条的方法
    private void updateSeekBar() {
        //(1)获取当前歌曲的总时长
        final int duration = mediaPlayer.getDuration();
        //(2)一秒钟获取一次当前进度
        final Timer timer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //(3)获取当前歌曲的进度
                int currentPosition = mediaPlayer.getCurrentPosition();
                //(4)创建Message对象
                Message msg = Message.obtain();
                //(5)使用msg携带多个数据
                Bundle bundle = new Bundle();
                bundle.putInt("duration", duration);
                bundle.putInt("currentPosition", currentPosition);
                msg.setData(bundle);
                //发送消息
                MainActivity.handler.sendMessage(msg);
            }
        };
        //300毫秒后每隔1秒钟获取一次当前歌曲的进度
        timer.schedule(task, 300, 1 * 1000);
        //(3)当歌曲播放完成的时候 把timer和task取消
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i("Tag", "歌曲播放完成了");
                timer.cancel();
                task.cancel();
            }
        });
    }

    //音乐暂停了
    public void pauseMusic(){
        Log.i("Tag", "音乐暂停了");
        mediaPlayer.pause();
    }

    //音乐继续播放了
    public void replayMusic(){
        Log.i("Tag", "音乐继续播放了");
        mediaPlayer.start();
    }

    //1.定义一个中间人对象（IBinder）
    private class MyBinder extends Binder implements IService{

        //调用播放音乐的方法
        @Override
        public void callPlayMusic() {
            playMusic();
        }

        //调用暂停音乐的方法
        @Override
        public void callStopMusic() {
            pauseMusic();
        }

        //调用继续播放音乐的方法
        @Override
        public void callReplayMusic() {
            replayMusic();
        }

        //调用设置播放音乐到指定位置的方法
        @Override
        public void callSeekToPosition(int position) {
            seekToPosition(position);
        }
    }

}
