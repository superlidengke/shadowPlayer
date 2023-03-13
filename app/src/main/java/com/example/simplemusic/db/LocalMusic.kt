package com.example.simplemusic.db

import org.litepal.crud.LitePalSupport

data class LocalMusic(
    var songUrl: String,
    var title: String,
    var artist: String,
    var imgUrl: String
) : LitePalSupport()