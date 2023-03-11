package com.example.simplemusic.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.simplemusic.R
import com.example.simplemusic.adapter.MusicAdapter
import com.example.simplemusic.adapter.PlayingMusicAdapter
import com.example.simplemusic.bean.Music
import com.example.simplemusic.db.LocalMusic
import com.example.simplemusic.service.MusicService
import com.example.simplemusic.service.MusicService.MusicServiceBinder
import com.example.simplemusic.service.OnStateChangeListener
import com.example.simplemusic.service.ServiceListener
import com.example.simplemusic.util.Utils
import me.rosuh.filepicker.bean.FileItemBeanImpl
import me.rosuh.filepicker.config.AbstractFileFilter
import me.rosuh.filepicker.config.FilePickerManager
import me.rosuh.filepicker.filetype.AudioFileType
import org.litepal.LitePal
import java.io.File


class LocalMusicActivity : AppCompatActivity(), View.OnClickListener {
    private var musicCountView: TextView? = null
    private var musicListView: ListView? = null
    var playingTitleView: TextView? = null
        private set
    var playingArtistView: TextView? = null
        private set
    var playingImgView: ImageView? = null
        private set
    var btnPlayOrPause: ImageView? = null
        private set
    private var localMusicList: MutableList<Music>? = null
    private var adapter: MusicAdapter? = null
    private var serviceBinder: MusicServiceBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_localmusic)

        //初始化
        initActivity()

        // 列表项点击事件
        musicListView!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                val music = localMusicList!![position]
                serviceBinder!!.addPlayList(music)
            }

        //列表项中更多按钮的点击事件
        adapter!!.setOnMoreButtonListener(object :
            MusicAdapter.OnMoreButtonListener {
            override fun onClick(i: Int) {
                val music = localMusicList!![i]
                val items = arrayOf("收藏到我的音乐", "添加到播放列表", "删除")
                val builder = AlertDialog.Builder(this@LocalMusicActivity)
                builder.setTitle(music.title + "-" + music.artist)
                builder.setItems(items) { dialog, which ->
                    when (which) {
                        1 -> serviceBinder!!.addPlayList(music)
                        2 -> {
                            //从列表和数据库中删除
                            localMusicList!!.removeAt(i)
                            LitePal.deleteAll(
                                LocalMusic::class.java,
                                "title=?",
                                music.title
                            )
                            adapter!!.notifyDataSetChanged()
                            musicCountView!!.text =
                                "播放全部(共" + localMusicList!!.size + "首)"
                        }
                    }
                }
                builder.create().show()
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.play_all -> serviceBinder!!.addPlayList(localMusicList!!)
            R.id.refresh -> {
                val aFilter = object : AbstractFileFilter() {
                    override fun doFilter(listData: ArrayList<FileItemBeanImpl>): ArrayList<FileItemBeanImpl> {
                        return ArrayList(listData.filter { item ->
                            item.isDir || AudioFileType().verify(item.fileName)
                        })
                    }
                }
                FilePickerManager.from(this)
                    .enableSingleChoice()
                    .filter(aFilter)
                    .skipDirWhenSelect(false)
                    .setCustomRootPath(getSaveAudioPath()!!)
                    .forResult(FilePickerManager.REQUEST_CODE)
            }
            R.id.player -> {
                val intent =
                    Intent(this@LocalMusicActivity, PlayerActivity::class.java)
                startActivity(intent)
                overridePendingTransition(
                    R.anim.bottom_in,
                    R.anim.bottom_silent
                )
            }
            R.id.play_or_pause -> serviceBinder!!.playOrPause()
            R.id.playing_list -> showPlayList()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FilePickerManager.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val list = FilePickerManager.obtainData()
                    val first = File(list[0])
                    val audioDirPath = if (first.isDirectory) {
                        first.absolutePath
                    } else {
                        first.parentFile.absolutePath
                    }

                    val editor = getSharedPreferences(
                        this.packageName,
                        Context.MODE_PRIVATE
                    ).edit()
                    editor.putString("audioDirPath", audioDirPath)
                    editor.commit()
                    initPlayList(audioDirPath)

                } else {
                    Toast.makeText(this, "None selected~", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        localMusicList!!.clear()
        unbindService(mServiceConnection)
    }

    private fun initActivity() {
        //初始化控件
        val btn_playAll = findViewById<ImageView>(R.id.play_all)
        musicCountView = findViewById(R.id.play_all_title)
        val btn_refresh = findViewById<ImageView>(R.id.refresh)
        musicListView = findViewById(R.id.music_list)
        val playerToolView = findViewById<RelativeLayout>(R.id.player)
        playingImgView = findViewById(R.id.playing_img)
        playingTitleView = findViewById(R.id.playing_title)
        playingArtistView = findViewById(R.id.playing_artist)
        btnPlayOrPause = findViewById(R.id.play_or_pause)
        val btn_playingList = findViewById<ImageView>(R.id.playing_list)
        btn_playAll.setOnClickListener(this)
        btn_refresh.setOnClickListener(this)
        playerToolView.setOnClickListener(this)
        btnPlayOrPause!!.setOnClickListener(this)
        btn_playingList.setOnClickListener(this)
        localMusicList = ArrayList()

        //绑定播放服务
        val i = Intent(this, MusicService::class.java)
        bindService(i, mServiceConnection, BIND_AUTO_CREATE)

        // 使用ToolBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "本地音乐"
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setSupportActionBar(toolbar)
        val sdCardRoot = Environment.getExternalStorageDirectory()
        Log.d("myApp", sdCardRoot.absolutePath)
        val dir = getSaveAudioPath()
        initPlayList(dir);
    }

    private fun getSaveAudioPath() =
        getSharedPreferences(this.packageName, Context.MODE_PRIVATE)
            .getString(
                "audioDirPath",
                Environment.getExternalStorageDirectory().absolutePath
            )


    private fun initPlayList(fileDir: String?) {
        if (fileDir == null) {
            return
        }
        //从数据库获取保存的本地音乐列表
        val list: MutableList<LocalMusic> = ArrayList()
        var path = ""

        val dir = File(fileDir)
        if (dir.exists()) {
            if (dir.listFiles() != null) {
                for (f in dir.listFiles()) {
                    if (f.isFile) path = f.absolutePath
                    if (f.name.contains(".mp3")) {
                        list.add(LocalMusic(path, f.name, "art", "null", false))
                        Log.i("audio path", path)
                    }
                }
            }
        }
        localMusicList?.clear()
        for (s in list) {
            val m =
                Music(s.songUrl, s.title, s.artist, s.imgUrl, s.isOnlineMusic)
            localMusicList!!.add(m)
        }

        // 本地音乐列表绑定适配器
        adapter = MusicAdapter(this, R.layout.music_item, localMusicList)
        musicListView!!.setAdapter(adapter)
        musicCountView!!.setText("播放全部(共" + localMusicList!!.size + "首)")
    }

    // 显示当前正在播放的音乐
    private fun showPlayList() {
        val builder = AlertDialog.Builder(this)

        //设计对话框的显示标题
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
            builder.setAdapter(playingAdapter) { dialog, which ->
                //监听列表项点击事件
                serviceBinder!!.addPlayList(playingList[which]!!)
            }

            //列表项中删除按钮的点击事件
            playingAdapter.setOnDeleteButtonListener(object :
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

        //设置该对话框是可以自动取消的，例如当用户在空白处随便点击一下，对话框就会关闭消失
        builder.setCancelable(true)

        //创建并显示对话框
        builder.create().show()
    }

    // 定义与服务的连接的匿名类
    private val mServiceConnection: ServiceConnection =
        object : ServiceConnection {
            // 绑定成功时调用
            override fun onServiceConnected(
                name: ComponentName,
                service: IBinder
            ) {

                // 绑定成功后，取得MusicSercice提供的接口
                serviceBinder = service as MusicServiceBinder

                // 注册监听器
                serviceBinder!!.registerOnStateChangeListener(listener)
                val item = serviceBinder!!.getCurrentMusic()
                if (serviceBinder!!.isPlaying) {
                    // 如果正在播放音乐, 更新控制栏信息
                    btnPlayOrPause!!.setImageResource(R.drawable.zanting)
                    playingTitleView!!.text = item!!.title
                    playingArtistView!!.text = item.artist
                    if (item.isOnlineMusic) {
                        Glide.with(applicationContext)
                            .load(item.imgUrl)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(playingImgView!!)
                    } else {
                        val resolver = contentResolver
                        val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
                        Glide.with(applicationContext)
                            .load(img)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(playingImgView!!)
                    }
                } else if (item != null) {
                    // 当前有可播放音乐但没有播放
                    btnPlayOrPause!!.setImageResource(R.drawable.bofang)
                    playingTitleView!!.text = item.title
                    playingArtistView!!.text = item.artist
                    if (item.isOnlineMusic) {
                        Glide.with(applicationContext)
                            .load(item.imgUrl)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(playingImgView!!)
                    } else {
                        val resolver = contentResolver
                        val img = Utils.getLocalMusicBmp(resolver, item.imgUrl)
                        Glide.with(applicationContext)
                            .load(img)
                            .placeholder(R.drawable.defult_music_img)
                            .error(R.drawable.defult_music_img)
                            .into(playingImgView!!)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                // 断开连接时注销监听器
                serviceBinder!!.unregisterOnStateChangeListener(listener)
            }
        }

    // 实现监听器监听MusicService的变化，
    private val listener: OnStateChangeListener = ServiceListener(this)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}