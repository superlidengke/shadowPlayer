package com.example.simplemusic.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.example.simplemusic.bean.Music
import com.example.simplemusic.db.PlayingMusic
import com.example.simplemusic.util.Utils
import org.litepal.LitePal
import java.io.IOException

class MusicService : Service() {
    var player: MediaPlayer? = null
        private set
    private var playingMusicList: MutableList<Music>? = null
    private var listenrList: MutableList<OnStateChangeListener>? = null
    private var binder: MusicServiceBinder? = null
    private var audioManager: AudioManager? = null
    private var currentMusicInner // 当前就绪的音乐
            : Music? = null
    var isAutoPlayAfterFocus // 获取焦点之后是否自动播放
            = false
    var isNeedReload // 播放时是否需要重新加载
            = false
    var playModeInner = Utils.TYPE_SINGLE // 播放模式
        private set
    private val spf: SharedPreferences? = null
    override fun onCreate() {
        super.onCreate()
        initPlayList() //初始化播放列表
        listenrList = ArrayList() //初始化监听器列表
        player = MediaPlayer() //初始化播放器
        player!!.setOnCompletionListener(onCompletionListener) //设置播放完成的监听器
        binder = MusicServiceBinder()
        audioManager =
            getSystemService(AUDIO_SERVICE) as AudioManager //获得音频管理服务
        val currentIdx = getSharedPreferences(this.packageName, MODE_PRIVATE)
            .getInt("currentIdx", -1)
        if (currentIdx != -1 && playingMusicList!!.size > currentIdx) {
            currentMusicInner = playingMusicList!![currentIdx]
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player!!.isPlaying) {
            player!!.stop()
        }
        player!!.release()
        playingMusicList!!.clear()
        listenrList!!.clear()
        handler.removeMessages(66)
        audioManager!!.abandonAudioFocus(audioFocusListener) //注销音频管理服务
    }

    //定义binder与活动通信
    inner class MusicServiceBinder : Binder() {
        // 添加一首歌曲
        fun addPlayList(item: Music) {
            addPlayListInner(item)
        }

        // 添加多首歌曲
        fun addPlayList(items: List<Music>) {
            addPlayListInner(items)
        }

        // 移除一首歌曲
        fun removeMusic(i: Int) {
            removeMusicInner(i)
        }

        fun playOrPause() {
            if (player!!.isPlaying) {
                pauseInner()
            } else {
                playInner()
            }
        }

        // 下一首
        fun playNext() {
            playNextInner()
        }

        // 上一首
        fun playPre() {
            playPreInner()
        }

        // 获取当前播放模式
        fun getPlayMode(): Int {
            return playModeInner
        }

        // 设置播放模式
        fun setPlayMode(mode: Int) {
            playModeInner = mode
        }

        // 设置播放器进度
        fun seekTo(pos: Int) {
            seekToInner(pos)
        }

        // 获取当前就绪的音乐
        fun getCurrentMusic(): Music? {
            return currentMusicInner
        }

        // 获取播放器播放状态
        val isPlaying: Boolean
            get() = isPlayingInner

        // 获取播放列表
        val playingList: List<Music?>?
            get() = playingListInner

        // 注册监听器
        fun registerOnStateChangeListener(l: OnStateChangeListener) {
            listenrList!!.add(l)
        }

        // 注销监听器
        fun unregisterOnStateChangeListener(l: OnStateChangeListener) {
            listenrList!!.remove(l)
        }
    }

    private fun addPlayListInner(music: Music) {
        if (!playingMusicList!!.contains(music)) {
            playingMusicList!!.add(0, music)
            val playingMusic = PlayingMusic(
                music.songUrl,
                music.title,
                music.artist,
                music.imgUrl
            )
            playingMusic.save()
        }
        currentMusicInner = music
        isNeedReload = true
        playInner()
    }

    private fun addPlayListInner(musicList: List<Music>) {
        playingMusicList!!.clear()
        LitePal.deleteAll(PlayingMusic::class.java)
        playingMusicList!!.addAll(musicList)
        for (i in musicList) {
            val playingMusic = PlayingMusic(
                i.songUrl,
                i.title,
                i.artist,
                i.imgUrl
            )
            playingMusic.save()
        }
        currentMusicInner = playingMusicList!![0]
        playInner()
    }

    private fun removeMusicInner(i: Int) {
        LitePal.deleteAll(
            PlayingMusic::class.java,
            "title=?",
            playingMusicList!![i].title
        )
        playingMusicList!!.removeAt(i)
    }

    fun playInner() {

        //获取音频焦点
        audioManager!!.requestAudioFocus(
            audioFocusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        //如果之前没有选定要播放的音乐，就选列表中的第一首音乐开始播放
        if (currentMusicInner == null && playingMusicList!!.size > 0) {
            currentMusicInner = playingMusicList!![0]
            isNeedReload = true
        }
        playMusicItem(currentMusicInner, isNeedReload)
        val currentIdx = playingMusicList!!.indexOf(currentMusicInner)
        val editor = getSharedPreferences(this.packageName, MODE_PRIVATE).edit()
        editor.putInt("currentIdx", currentIdx)
        editor.putInt("currentLoopCount", Utils.currentLoopCount)
        editor.commit()
    }

    fun pauseInner() {
        player!!.pause()
        for (l in listenrList!!) {
            l.onPause()
        }
        // 暂停后不需要重新加载
        isNeedReload = false
    }

    private fun playPreInner() {
        //获取当前播放（或者被加载）音乐的上一首音乐
        //如果前面有要播放的音乐，把那首音乐设置成要播放的音乐
        val currentIndex = playingMusicList!!.indexOf(currentMusicInner)
        if (currentIndex - 1 >= 0) {
            Utils.currentLoopCount = 0
            currentMusicInner = playingMusicList!![currentIndex - 1]
            isNeedReload = true
            playInner()
        }
    }

    fun playNextInner() {
        if (playModeInner == Utils.TYPE_RANDOM) {
            //随机播放
            val i = (Math.random() * playingMusicList!!.size).toInt()
            currentMusicInner = playingMusicList!![i]
        } else {
            //loop play list
            val currentIndex = playingMusicList!!.indexOf(currentMusicInner)
            if (currentIndex < playingMusicList!!.size - 1) {
                currentMusicInner = playingMusicList!![currentIndex + 1]
            } else {
                currentMusicInner = playingMusicList!![0]
            }
        }
        isNeedReload = true
        Utils.currentLoopCount = 0
        playInner()
    }

    private fun seekToInner(pos: Int) {
        //将音乐拖动到指定的时间
        player!!.seekTo(pos)
    }

    private val isPlayingInner: Boolean
        private get() = player!!.isPlaying
    val playingListInner: List<Music?>?
        get() = playingMusicList

    // 将要播放的音乐载入MediaPlayer，但是并不播放
    private fun prepareToPlay(item: Music) {
        try {
            player!!.reset()
            //设置播放音乐的地址
            player!!.setDataSource(this@MusicService, Uri.parse(item.songUrl))
            //准备播放音乐
            player!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 播放音乐，根据reload标志位判断是非需要重新加载音乐
    private fun playMusicItem(item: Music?, reload: Boolean) {
        if (item == null) {
            return
        }
        if (reload) {
            //需要重新加载音乐
            prepareToPlay(item)
        }
        player!!.start()
        for (l in listenrList!!) {
            l.onPlay(item)
        }
        isNeedReload = true

        //移除现有的更新消息，重新启动更新
        handler.removeMessages(66)
        handler.sendEmptyMessage(66)
    }

    // 初始化播放列表
    private fun initPlayList() {
        playingMusicList = mutableListOf<Music>()
        val list = LitePal.findAll(
            PlayingMusic::class.java
        )
        for (i in list) {
            val m =
                Music(i.songUrl, i.title, i.artist, i.imgUrl)
            playingMusicList?.add(m)
        }
        if (!playingMusicList.isNullOrEmpty()) {
            currentMusicInner = playingMusicList?.get(0)
            isNeedReload = true
        }
    }

    //当前歌曲播放完成的监听器
    private val onCompletionListener: OnCompletionListener =
        MyOnCompletionListener(this)
    private val audioFocusListener: OnAudioFocusChangeListener =
        MyOnAudioFocusChangeListener(this)

    @SuppressLint("HandlerLeak")
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                66 -> {
                    //通知监听者当前的播放进度
                    val played = player!!.currentPosition.toLong()
                    val duration = player!!.duration.toLong()
                    for (l in listenrList!!) {
                        l.onPlayProgressChange(played, duration)
                    }
                    //间隔一秒发送一次更新播放进度的消息
                    sendEmptyMessageDelayed(66, 1000)
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        //当组件bindService()之后，将这个Binder返回给组件使用
        return binder
    } //焦点控制
}