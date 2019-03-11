package com.test.opengl

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class CommonAdapter<T>(private val mContext: Context, private val layoutId: Int, private val mDatas: List<T>) : RecyclerView.Adapter<GLViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GLViewHolder {
        val iteView = LayoutInflater.from(mContext).inflate(layoutId, null)
        return GLViewHolder(iteView)
    }

    override fun onBindViewHolder(glViewHolder: GLViewHolder, i: Int) {
        convert(glViewHolder, mDatas[i], i)
    }

    protected abstract fun convert(glViewHolder: GLViewHolder, t: T, position: Int)

    override fun getItemCount(): Int {
        return mDatas.size
    }
}
