package com.shashi.jobsforherassignment.activity

import android.os.Bundle
import android.view.MenuItem
import com.shashi.jobsforherassignment.R
import com.shashi.jobsforherassignment.base.BaseActivity
import kotlinx.android.synthetic.main.activity_1.*

class Activity1 : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)
        supportActionBar?.title = "Activity 1"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val bundle = intent.extras
        if (bundle != null) {
            for (key in bundle.keySet()) {
                val value = bundle[key]
                textView.text = "Inside$value"
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}