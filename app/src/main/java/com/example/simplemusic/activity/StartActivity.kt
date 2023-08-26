package com.example.simplemusic.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemusic.R

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_start)
        // delay SPLASH_DISPLAY_LENGTH, then move to MainActivity
        val splashDisplayLength = 1000
        Handler(Looper.getMainLooper()).postDelayed({
            val intent =
                Intent(this@StartActivity, LocalMusicActivity::class.java)
//            val intent = Intent(this@StartActivity, DrawActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDisplayLength.toLong())
    }
}