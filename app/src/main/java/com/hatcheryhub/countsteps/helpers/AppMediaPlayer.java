package com.hatcheryhub.countsteps.helpers;

import android.media.MediaPlayer;

import com.hatcheryhub.countsteps.R;

/**
 * Created by aditya on 12/12/15.
 */
public class AppMediaPlayer {

    public static void playNewMessageSound(){
        MediaPlayer mp = MediaPlayer.create(Phantom.getInstance(), R.raw.sounds_856_hit);
        mp.start();
    }
}
