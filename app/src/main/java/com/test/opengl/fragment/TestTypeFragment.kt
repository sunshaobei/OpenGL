package com.test.opengl.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.NavHostFragment

import com.test.opengl.R

/**
 * A simple [Fragment] subclass.
 *
 */
class TestTypeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var mView = inflater.inflate(R.layout.fragment_test_type, container, false)

        var btn_shape  = mView.findViewById<Button>(R.id.btn_shape)
        var btn_operation  = mView.findViewById<Button>(R.id.btn_operation)
        var btn_egl  = mView.findViewById<Button>(R.id.btn_egl)
        btn_shape.setOnClickListener {
            NavHostFragment.findNavController(this@TestTypeFragment).navigate(R.id.nav_shape)
        }
        btn_operation.setOnClickListener {
            NavHostFragment.findNavController(this@TestTypeFragment).navigate(R.id.nav_gesture_list)
        }
        btn_egl.setOnClickListener {
            NavHostFragment.findNavController(this@TestTypeFragment).navigate(R.id.nav_egl)
        }

        return mView
    }

}
