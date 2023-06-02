package com.example.simplemusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.simplemusic.R
import com.example.simplemusic.bean.Music

class PlayingMusicAdapter(
    context: Context,
    private val resId: Int,
    private val mData: List<Music>
) : BaseAdapter() {
    private val mInflater: LayoutInflater
    private var monDeleteButtonListener: OnDeleteButtonListener? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val item = mData[position]
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view = mInflater.inflate(resId, parent, false)
            holder = ViewHolder()
            holder.title = view.findViewById(R.id.playingmusic_title)
            holder.artist = view.findViewById(R.id.playingmusic_artist)
            holder.delete = view.findViewById(R.id.delete)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.title!!.text = item.title
        holder.artist!!.text = item.artist
        holder.delete!!.setOnClickListener {
            monDeleteButtonListener!!.onClick(position)
        }
        return view
    }

    internal inner class ViewHolder {
        var title: TextView? = null
        var artist: TextView? = null
        var delete: ImageView? = null
    }

    interface OnDeleteButtonListener {
        fun onClick(i: Int)
    }

    fun setOnDeleteButtonListener(monDeleteButtonListener: OnDeleteButtonListener?) {
        this.monDeleteButtonListener = monDeleteButtonListener
    }
}