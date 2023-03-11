package com.example.simplemusic.service

import com.example.simplemusic.bean.Music

interface OnStateChangeListener {
    fun onPlayProgressChange(played: Long, duration: Long) //播放进度变化
    fun onPlay(item: Music) //播放状态变化
    fun onPause() //播放状态变化
}