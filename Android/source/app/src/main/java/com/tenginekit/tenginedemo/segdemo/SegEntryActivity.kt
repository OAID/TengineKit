package com.tenginekit.tenginedemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SegEntryActivity : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seg_entry)
        val view = findViewById<View>(R.id.bitmap)
        view.setOnClickListener(this)
    }

    private fun jumpToBitmapActivity() {
        val intent = Intent(this, SegBitmapActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(view: View?) {
        jumpToBitmapActivity()
    }


}
