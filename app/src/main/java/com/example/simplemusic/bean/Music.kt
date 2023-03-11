package com.example.simplemusic.bean

class Music(
    var songUrl: String,
    var title: String,
    var artist: String,
    var imgUrl: String,
    var isOnlineMusic: Boolean
) {
    // compare with title
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        return title == (o as Music).title
    }
}