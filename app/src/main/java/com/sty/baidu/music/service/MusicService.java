package com.sty.baidu.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 音乐播放的服务
 * Created by Steven.T on 2017/12/13/0013.
 */

public class MusicService extends Service {
    //2.把定义的中间人对象返回
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    //服务第一次开启时调用
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //当服务销毁的时候调用
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //音乐播放了
    public void playMusic(){
        Log.i("Tag", "音乐播放了");
        //TODO 以后实现
    }

    //音乐暂停了
    public void pauseMusic(){
        Log.i("Tag", "音乐暂停了");
    }

    //音乐继续播放了
    public void replayMusic(){
        Log.i("Tag", "音乐继续播放了");
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
    }

}
