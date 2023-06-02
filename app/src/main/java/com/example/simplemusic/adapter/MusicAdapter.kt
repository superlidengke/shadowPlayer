package com.example.simplemusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.simplemusic.R
import com.example.simplemusic.bean.Music

class MusicAdapter(
    context: Context,
    resId: Int,
    private val mData: List<Music>?
) : BaseAdapter() {
    private val mInflater: LayoutInflater
    private val mResource: Int
    private var monMoreButtonListener: OnMoreButtonListener? = null

    init {
        mInflater = LayoutInflater.from(context)
        mResource = resId
    }

    override fun getCount(): Int {
        return mData?.size ?: 0
    }

    override fun getItem(position: Int): Any {
        return mData?.get(position)!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val item = mData!![position]
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false)
            holder = ViewHolder()
            holder.title = view.findViewById(R.id.music_title)
            holder.artist = view.findViewById(R.id.music_artist)
            holder.more = view.findViewById(R.id.more)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.title!!.text = item.title
        holder.artist!!.text = item.artist
        holder.more!!.setOnClickListener {
            monMoreButtonListener!!.onClick(
                position
            )
        }
        return view
    }

    internal inner class ViewHolder {
        var title: TextView? = null
        var artist: TextView? = null
        var more: LinearLayout? = null
    }

    interface OnMoreButtonListener {
        fun onClick(i: Int)
    }

    fun setOnMoreButtonListener(monMoreButtonListener: OnMoreButtonListener?) {
        this.monMoreButtonListener = monMoreButtonListener
    }
}