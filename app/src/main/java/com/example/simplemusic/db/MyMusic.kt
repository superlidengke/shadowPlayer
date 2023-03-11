package com.example.simplemusic.db

import org.litepal.crud.LitePalSupport

class MyMusic(//歌曲地址
    var songUrl: String, //歌曲名
    var title: String, //歌手
    var artist: String, var imgUrl: String, var isOnlineMusic: Boolean
) : LitePalSupport()