package com.planit.mobile

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView

import com.planit.mobile.R

/**
 * Created by Home on 2018-07-26.
 */

class CustomWindow : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var title: TextView

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)

        setContentView(R.layout.activity_main)

        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title)

        title = findViewById<TextView>(R.id.title)
        title.text = "PlanIt"

    }
}
