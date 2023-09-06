package com.example.simplemusic.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.simplemusic.R

class PlayCounter : LinearLayout {

    private var playCountView: Spinner? = null
    private var currentLoopCountView: TextView? = null

    private val KEY_SUPER_STATE = "super_state"
    private val KEY_PLAY_COUNT = "play_count"

    private var totalLoopCounter = 10

    constructor(ctx: Context) : super(ctx) {
        initView()
    }

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        initView()
    }

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.play_counter, this)
        currentLoopCountView = findViewById(R.id.current_loop_count)
        playCountView = findViewById(R.id.play_count)
        orientation = HORIZONTAL
    }

    // used for orientation change
    
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(KEY_SUPER_STATE, super.onSaveInstanceState())
        bundle.putInt(
            KEY_PLAY_COUNT,
            playCountView?.selectedItem.toString().toInt()
        )
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            totalLoopCounter = state.getInt(KEY_PLAY_COUNT)
            super.onRestoreInstanceState(state.getParcelable(KEY_SUPER_STATE))
        } else {
            super.onRestoreInstanceState(state)
        }

    }
}