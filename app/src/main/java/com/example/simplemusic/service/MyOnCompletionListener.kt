package com.example.simplemusic.service

import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import com.example.simplemusic.util.Utils

class MyOnCompletionListener internal constructor(private val service: MusicService) :
    OnCompletionListener {
    override fun onCompletion(mp: MediaPlayer) {
        Utils.count++ //累计听歌数量+1
        Utils.currentLoopCount++
        if (service.playModeInner == Utils.TYPE_SINGLE && Utils.currentLoopCount < Utils.totalLoopTimes) {
            //单曲循环
            service.isNeedReload = true
            service.playInner()
        } else {
            Utils.currentLoopCount = 0
            service.playNextInner()
        }
    }
}