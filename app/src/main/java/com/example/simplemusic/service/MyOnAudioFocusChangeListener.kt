package com.example.simplemusic.service

import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener

class MyOnAudioFocusChangeListener internal constructor(private val service: MusicService) :
    OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> if (service.player!!.isPlaying) {
                //会长时间失去，所以告知下面的判断，获得焦点后不要自动播放
                service.isAutoPlayAfterFocus = false
                service.pauseInner() //因为会长时间失去，所以直接暂停
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> if (service.player!!.isPlaying) {
                //短暂失去焦点，先暂停。同时将标志位置成重新获得焦点后就开始播放
                service.isAutoPlayAfterFocus = true
                service.pauseInner()
            }
            AudioManager.AUDIOFOCUS_GAIN ->                 //重新获得焦点，且符合播放条件，开始播放
                if (!service.player!!.isPlaying && service.isAutoPlayAfterFocus) {
                    service.isAutoPlayAfterFocus = false
                    service.playInner()
                }
        }
    }
}