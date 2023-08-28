package com.example.simplemusic.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.simplemusic.R
import com.example.simplemusic.adapter.PlayingMusicAdapter
import com.example.simplemusic.bean.Music
import com.example.simplemusic.service.MusicService
import com.example.simplemusic.service.MusicService.MusicServiceBinder
import com.example.simplemusic.service.OnStateChangeListener
import com.example.simplemusic.util.Utils
import com.example.simplemusic.view.MyView
import com.example.simplemusic.view.RotateAnimator


class PlayerActivity : AppCompatActivity(), View.OnClickListener,
    AdapterView.OnItemSelectedListener {
    private var musicTitleView: TextView? = null
    private var musicArtistView: TextView? = null
    private var musicImgView: ImageView? = null
    private var btnPlayMode: ImageView? = null
    private var btnPlayPre: ImageView? = null
    private var btnPlayOrPause: ImageView? = null
    private var btnPlayNext: ImageView? = null
    private var btnPlayingList: ImageView? = null
    private var nowTimeView: TextView? = null
    private var totalTimeView: TextView? = null
    private var seekBar: SeekBar? = null
    private var rotateAnimator: RotateAnimator? = null
    private var serviceBinder: MusicServiceBinder? = null
    private var playCountView: Spinner? = null
    private var currentLoopCountView: TextView? = null
    private var waveFormView: MyView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        Utils.currentLoopCount =
            getSharedPreferences(this.packageName, MODE_PRIVATE)
                .getInt("currentLoopCount", 0)

        //初始化
        initActivity()


    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
    }

    // 控件监听
    override fun onClick(v: View) {
        when (v.id) {
            R.id.play_mode -> {
                // 改变播放模式
                val mode = serviceBinder!!.getPlayMode()
                Log.i("PlayMode", mode.toString())
                when (mode) {
                    Utils.TYPE_ORDER -> {
                        serviceBinder!!.setPlayMode(Utils.TYPE_SINGLE)
                        Toast.makeText(
                            this@PlayerActivity,
                            "单曲循环",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnPlayMode!!.setImageResource(R.drawable.ic_singlerecycler)
                    }

                    Utils.TYPE_SINGLE -> {
                        serviceBinder!!.setPlayMode(Utils.TYPE_RANDOM)
                        Toast.makeText(
                            this@PlayerActivity,
                            "随机播放",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnPlayMode!!.setImageResource(R.drawable.ic_random)
                    }

                    else -> {
                        //                    case Utils.TYPE_RANDOM:
                        serviceBinder!!.setPlayMode(Utils.TYPE_ORDER)
                        Toast.makeText(
                            this@PlayerActivity,
                            "列表循环",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnPlayMode!!.setImageResource(R.drawable.ic_playrecycler)
                    }
                }
            }

            R.id.play_pre ->                 // 上一首
                serviceBinder!!.playPre()

            R.id.play_next ->                 // 下一首
                serviceBinder!!.playNext()

            R.id.play_or_pause ->                 // 播放或暂停
                serviceBinder!!.playOrPause()

            R.id.playing_list ->                 // 播放列表
                showPlayList()

            else -> {}
        }
    }

    private fun initActivity() {
        musicTitleView = findViewById(R.id.title)
        musicArtistView = findViewById(R.id.artist)
        musicImgView = findViewById(R.id.imageView)
        btnPlayMode = findViewById(R.id.play_mode)
        btnPlayOrPause = findViewById(R.id.play_or_pause)
        btnPlayPre = findViewById(R.id.play_pre)
        btnPlayNext = findViewById(R.id.play_next)
        btnPlayingList = findViewById(R.id.playing_list)
        seekBar = findViewById(R.id.seekbar)
        nowTimeView = findViewById(R.id.current_time)
        totalTimeView = findViewById(R.id.total_time)
        playCountView = findViewById(R.id.play_count)
        currentLoopCountView = findViewById(R.id.current_loop_count)
        currentLoopCountView?.text = Utils.currentLoopCount.toString()
        waveFormView = findViewById(R.id.wave_form)

        val needleView = findViewById<ImageView>(R.id.ivNeedle)

        // ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)

        // 设置监听
        btnPlayMode?.setOnClickListener(this)
        btnPlayOrPause?.setOnClickListener(this)
        btnPlayPre?.setOnClickListener(this)
        btnPlayNext?.setOnClickListener(this)
        btnPlayingList?.setOnClickListener(this)
        seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                //拖动进度条时
                nowTimeView?.text = Utils.formatTime(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                serviceBinder!!.seekTo(seekBar.progress)
            }
        })
        playCountView?.onItemSelectedListener = this
        playCountView?.setSelection(2)

        //初始化动画
        rotateAnimator = RotateAnimator(this, musicImgView, needleView)
        rotateAnimator!!.set_Needle()

        // 绑定service
        val i = Intent(this, MusicService::class.java)
        bindService(i, mServiceConnection, BIND_AUTO_CREATE)
    }

    //显示当前正在播放的音乐
    private fun showPlayList() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("播放列表")

        //获取播放列表
        val playingList = serviceBinder!!.playingList
        if (!playingList.isNullOrEmpty()) {
            //播放列表有曲目，显示所有音乐
            val playingAdapter = PlayingMusicAdapter(
                this,
                R.layout.playinglist_item,
                playingList
            )
            builder.setAdapter(playingAdapter) { _, which ->
                //监听列表项点击事件
                serviceBinder!!.addPlayList(playingList[which]!!)
            }

            //列表项中删除按钮的点击事件
            playingAdapter.setOnDeleteButtonListener(
                object :
                    PlayingMusicAdapter.OnDeleteButtonListener {
                    override fun onClick(i: Int) {
                        serviceBinder!!.removeMusic(i)
                        playingAdapter.notifyDataSetChanged()
                    }
                })
        } else {
            //播放列表没有曲目，显示没有音乐
            builder.setMessage("没有正在播放的音乐")
        }
        builder.setCancelable(true)
        builder.create().show()
    }

    //定义与服务的连接的匿名类
    private val mServiceConnection: ServiceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                service: IBinder
            ) {
                //绑定成功后，取得MusicSercice提供的接口
                serviceBinder = service as MusicServiceBinder

                //注册监听器
                serviceBinder!!.registerOnStateChangeListener(listenr)

                //获得当前音乐
                val item = serviceBinder!!.getCurrentMusic()
                if (item == null) {
                    //当前音乐为空, seekbar不可拖动
                    seekBar!!.isEnabled = false
                } else {
                    // redraw wave
                    waveFormView?.soundPath = item.songUrl
                    waveFormView?.invalidate()
                    if (serviceBinder!!.isPlaying) {
                        //如果正在播放音乐, 更新信息
                        musicTitleView!!.text = item.title
                        musicArtistView!!.text = item.artist
                        btnPlayOrPause!!.setImageResource(R.drawable.ic_pause)
                        rotateAnimator!!.playAnimator()

                        val resolver = contentResolver
                        val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
                        Glide.with(applicationContext)
                            .load(img)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(musicImgView!!)

                    } else {
                        //当前有可播放音乐但没有播放
                        musicTitleView!!.text = item.title
                        musicArtistView!!.text = item.artist
                        btnPlayOrPause!!.setImageResource(R.drawable.ic_play)

                        val resolver = contentResolver
                        val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
                        Glide.with(applicationContext)
                            .load(img)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(musicImgView!!)

                    }
                }

                // 获取当前播放模式
                when (serviceBinder!!.getPlayMode()) {
                    Utils.TYPE_ORDER -> btnPlayMode!!.setImageResource(R.drawable.ic_playrecycler)
                    Utils.TYPE_SINGLE -> btnPlayMode!!.setImageResource(R.drawable.ic_singlerecycler)
                    Utils.TYPE_RANDOM -> btnPlayMode!!.setImageResource(R.drawable.ic_random)
                    else -> {}
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                //断开连接之后, 注销监听器
                serviceBinder!!.unregisterOnStateChangeListener(listenr)
            }
        }

    //实现监听器监听MusicService的变化，
    private val listenr: OnStateChangeListener =
        object : OnStateChangeListener {
            override fun onPlayProgressChange(played: Long, duration: Long) {
                seekBar!!.max = duration.toInt()
                totalTimeView!!.text = Utils.formatTime(duration)
                nowTimeView!!.text = Utils.formatTime(played)
                seekBar!!.progress = played.toInt()
            }

            override fun onPlay(item: Music) {
                // count view starts from 1
                currentLoopCountView!!.text =
                    (1 + Utils.currentLoopCount).toString();

                musicTitleView!!.text = item.title
                musicArtistView!!.text = item.artist
                btnPlayOrPause!!.setImageResource(R.drawable.ic_pause)
                rotateAnimator!!.playAnimator()

                waveFormView?.soundPath = item.songUrl
                waveFormView?.invalidate()

                val resolver = contentResolver
                val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
                Glide.with(applicationContext)
                    .load(img)
                    .placeholder(R.drawable.defult_music_img)
                    .error(R.drawable.defult_music_img)
                    .into(musicImgView!!)

            }

            override fun onPause() {
                //变为暂停状态时
                btnPlayOrPause!!.setImageResource(R.drawable.ic_play)
                rotateAnimator!!.pauseAnimator()
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        //界面退出时的动画
        overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out)
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        val item = parent?.getItemAtPosition(position)?.toString()
        if (item != null) {
            Utils.totalLoopTimes = item.toInt()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}