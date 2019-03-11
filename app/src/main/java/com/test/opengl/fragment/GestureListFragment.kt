package com.test.opengl.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.test.opengl.CommonAdapter
import com.test.opengl.GLViewHolder
import com.test.opengl.R
import com.test.opengl.constant.GestureData

class GestureListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = RecyclerView(context!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = GestureTypeAdapter(context!!)
        return recyclerView
    }


    internal inner class GestureTypeAdapter constructor(context:Context): CommonAdapter<GestureData>(context, R.layout.item_shape, GestureData.values().toList()) {
        override fun convert(glViewHolder: GLViewHolder, t: GestureData, position: Int) {
            var textView = glViewHolder.itemView.findViewById(R.id.tv) as TextView
            var name = t.getDataName()
            var c  = t.getC()
            textView.text = name
            glViewHolder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("c",c)
                NavHostFragment.findNavController(this@GestureListFragment).navigate(R.id.nav_gesture,bundle)
            }
        }
    }
}
