package com.tenginekit.tenginedemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tenginekit.tenginedemo.facedemo.FaceEntryActivity
import com.tenginekit.tenginedemo.segdemo.SegEntryActivity


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actvity_main)
        val face = findViewById<View>(R.id.faceEntry)
        val seg = findViewById<View>(R.id.segEntry)
        face.setOnClickListener(this)
        seg.setOnClickListener(this)
    }


    private fun jumpToFaceActivity() {
        val intent = Intent(this, FaceEntryActivity::class.java)
        startActivity(intent)
    }

    private fun jumpToSegActivity() {
        val intent = Intent(this, SegEntryActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.faceEntry -> {
                jumpToFaceActivity()
            }
            R.id.segEntry -> {
                jumpToSegActivity()
            }
        }
    }
}
