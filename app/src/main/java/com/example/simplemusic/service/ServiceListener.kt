package com.example.simplemusic.service

import com.bumptech.glide.Glide
import com.example.simplemusic.R
import com.example.simplemusic.activity.LocalMusicActivity
import com.example.simplemusic.bean.Music
import com.example.simplemusic.util.Utils

class ServiceListener(private val localMusicActivity: LocalMusicActivity) :
    OnStateChangeListener {
    override fun onPlayProgressChange(played: Long, duration: Long) {}
    override fun onPlay(item: Music) {
        localMusicActivity.btnPlayOrPause?.setImageResource(R.drawable.zanting)
        localMusicActivity.playingTitleView?.text = item.title
        localMusicActivity.playingArtistView?.text = item.artist
        localMusicActivity.btnPlayOrPause?.isEnabled = true
        val resolver = localMusicActivity.contentResolver
        val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
        Glide.with(localMusicActivity.applicationContext)
            .load(img)
            .placeholder(R.drawable.defult_music_img)
            .error(R.drawable.defult_music_img)
            .into(localMusicActivity.playingImgView!!)
    }

    override fun onPause() {
        localMusicActivity.btnPlayOrPause?.setImageResource(R.drawable.bofang)
        localMusicActivity.btnPlayOrPause?.isEnabled = true
    }
}