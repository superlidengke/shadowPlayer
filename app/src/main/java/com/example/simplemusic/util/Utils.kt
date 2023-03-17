package com.example.simplemusic.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    //累计听歌数量
    @JvmField
    var count = 0

    @JvmField
    var currentLoopCount // current song loop count
            = 0

    @JvmField
    var totalLoopTimes = 10 // total loop times

    //播放模式
    const val TYPE_ORDER = 4212 //顺序播放
    const val TYPE_SINGLE = 4313 //单曲循环
    const val TYPE_RANDOM = 4414 //随机播放

    // 获取本地音乐封面图片
    fun getLocalMusicBmp(res: ContentResolver, musicPic: String?): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            val uri = Uri.parse(musicPic)
            inputStream = res.openInputStream(uri)
            val sBitmapOptions = BitmapFactory.Options()
            BitmapFactory.decodeStream(inputStream, null, sBitmapOptions)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    //格式化歌曲时间
    fun formatTime(time: Long): String {
        val dateFormat = SimpleDateFormat("mm:ss")
        val data = Date(time)
        return dateFormat.format(data)
    }
}