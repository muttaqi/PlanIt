package com.planit.mobile.data

import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker

import com.planit.mobile.R

/**
 * Created by Home on 2018-07-30.
 * com.google.api.client.googleapis.auth.clientlogin.AuthKeyValueParser
 */

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var minute = 0
        internal set
    var hour = 0
        internal set

    lateinit var tpd: TimePickerDialog

    fun setTimes(hr: Int, min: Int) {

        this.hour = hr
        this.minute = min
    }

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {


        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

        // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
        tpd = TimePickerDialog(activity,
                R.style.Theme_Dialog, this, hour, minute, false)

        // Return the TimePickerDialog
        return tpd
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {

        tpd.onTimeChanged(view, hourOfDay, minute)
    }
}
