package com.example.simplemusic.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemusic.view.MyView

class DrawActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MyView(this))
    }
}