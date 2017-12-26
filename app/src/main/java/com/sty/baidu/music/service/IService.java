package com.sty.baidu.music.service;

/**
 * Created by Steven.T on 2017/12/13/0013.
 */

public interface IService {

    void callPlayMusic();
    void callStopMusic();
    void callReplayMusic();

    void callSeekToPosition(int position);
}
