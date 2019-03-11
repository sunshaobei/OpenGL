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
import com.test.opengl.constant.ShapeData
import com.test.opengl.R

class ShapeListFragment : Fragment() {

    private lateinit var recyclerView:RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!::recyclerView.isInitialized){
            recyclerView = RecyclerView(context!!)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ShapeTypeAdapter(context!!)
        }
        return recyclerView
    }


    internal inner class ShapeTypeAdapter constructor(context:Context): CommonAdapter<ShapeData>(context, R.layout.item_shape, ShapeData.values().toList()) {
        override fun convert(glViewHolder: GLViewHolder, t: ShapeData, position: Int) {
            var textView = glViewHolder.itemView.findViewById(R.id.tv) as TextView
            var name = t.getDataName()
            var c  = t.getC()
            textView.text = name
            glViewHolder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("c",c)
                NavHostFragment.findNavController(this@ShapeListFragment).navigate(R.id.navigation_to_triangle,bundle)
            }
        }
    }
}
