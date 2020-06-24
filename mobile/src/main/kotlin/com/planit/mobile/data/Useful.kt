package com.planit.mobile.data

import android.content.Context
import android.content.DialogInterface
import android.graphics.PorterDuff
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView

import com.planit.mobile.MainActivity
import com.planit.mobile.R

import java.lang.reflect.Field
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * Created by Home on 2017-08-02.
 */

class Useful {

    internal var formatter: DateFormat = SimpleDateFormat("hh:mm a")

    open class ThemedAlertDialog(v: View?, private val mContext: Context) {

        var adb: AlertDialog.Builder
        lateinit var ad: AlertDialog

        var alertReady = false

        init {

            adb = AlertDialog.Builder(mContext)
            //et = new EditText(c);

            if (v != null) {

                v.background.mutate().setColorFilter(mContext.resources.getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)

                adb.setView(v)
            }

            alertReady = false
        }

        fun addBtns(negBtn: String, posBtn: String) {

            adb
                    .setNegativeButton(negBtn) { dialogInterface, i -> }
                    .setPositiveButton(posBtn) { dialogInterface, i -> }
        }

        fun create() {

            ad = adb.create()
        }

        fun setBtnColor() {

            ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(mContext.resources.getColor(R.color.colorPrimary))
            ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(mContext.resources.getColor(R.color.colorPrimary))
        }
    }

    class NameAlertDialog : ThemedAlertDialog {

        lateinit var et: EditText
        lateinit var actv: AutoCompleteTextView

        constructor(whatToCreate: String, posBtn: String, negBtn: String, dfltTxt: String, et: EditText, c: Context) : super(et, c) {

            this.et = et

            try {
                val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                f.isAccessible = true
                f.set(et, R.drawable.et_cursor)
            } catch (ignored: Exception) {

                Log.d(TAG, "DEBUG USEFUL 239 $ignored")
            }

            et.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            et.setText(dfltTxt)

            super.addBtns(negBtn, posBtn)

            adb.setTitle("Enter " + whatToCreate.toLowerCase() + " name")
        }

        constructor(whatToCreate: String, posBtn: String, negBtn: String, dfltTxt: String, actv: AutoCompleteTextView, c: Context) : super(actv, c) {

            this.actv = actv

            try {
                val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                f.isAccessible = true
                f.set(actv, R.drawable.et_cursor)
            } catch (ignored: Exception) {

                Log.d(TAG, "DEBUG USEFUL 239 $ignored")
            }

            actv.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            actv.setText(dfltTxt)

            super.addBtns(negBtn, posBtn)

            adb.setTitle("Enter " + whatToCreate.toLowerCase() + " name")
        }
    }

    companion object {

        internal var TAG = Useful::class.java.simpleName

        // item1 subitem1 || item1 subitem2 // item2 subitem1 || item2 subitem2 // item3 etc


        fun concatLine(list: List<String>): String {

            var string = ""

            for (s in list) {

                if (s == list[list.size]) {

                    string += s
                } else {

                    string += "$s||"
                }
            }

            return string
        }

        fun concatLineAndSlash(list: List<List<String>>): String {

            var string = ""

            for (inList in list) {

                var inString = ""

                for (s in inList) {

                    if (s == inList[inList.size]) {

                        inString += s
                    } else {

                        inString += "$s||"
                    }
                }

                if (inList == list[list.size]) {

                    string += inString
                } else {

                    string += "$inString//"
                }
            }

            return string
        }

        fun removeUnderscores(list: MutableList<String>) {

            for (s in list) {

                if (s == "_") {
                    list.remove(s)
                }
            }

        }

        fun convertStringArrayToList(sArray: Array<String>): List<String> {

            val retList = ArrayList<String>()

            for (s in sArray) {

                retList.add(s)
            }

            return retList
        }

        fun upperCaseAbbrev(s: String): String {

            var retS = ""

            for (i in 0 until s.length) {

                if (Character.isUpperCase(s[i])) {

                    retS += s.substring(i, i + 1)
                }
            }

            return retS
        }

        fun debugID(TAG: String): String {
            return "DEBUG " + upperCaseAbbrev(TAG) + " " + Thread.currentThread().stackTrace[3].lineNumber + " "
        }

        fun doubleSingleQuotations(s: String): String {
            var s = s

            var i = 0
            while (i < s.length) {

                if (s.substring(i, i + 1) == "'") {

                    s = s.substring(0, i) + "'" + s.substring(i, s.length)
                    i++
                }
                i++
            }

            return s
        }

        fun slide_down(ctx: Context, v: View?) {

            val a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down)

            if (a != null) {

                a.reset()

                if (v != null) {

                    v.clearAnimation()
                    v.startAnimation(a)
                }
            }
        }

        fun slide_up(ctx: Context, v: View?) {

            val a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up)

            if (a != null) {

                a.reset()

                if (v != null) {

                    v.clearAnimation()
                    v.startAnimation(a)
                }
            }
        }

        fun expand(v: View) {
            v.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            val targetHeight = v.measuredHeight

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.layoutParams.height = 1
            v.visibility = View.VISIBLE
            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    v.layoutParams.height = if (interpolatedTime == 1f)
                        WindowManager.LayoutParams.WRAP_CONTENT
                    else
                        (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            // 1dp/ms
            a.duration = (3 * targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }

        fun collapse(v: View, setGone: Boolean) {
            val initialHeight = v.measuredHeight

            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        v.visibility = View.GONE
                    } else {
                        v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }


                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            a.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {

                    if (setGone) {

                        MainActivity.famMain.visibility = View.GONE
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })

            // 1dp/ms
            a.duration = (3 * initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }

        fun rotate_down(ctx: Context, v: View?) {

            val a = AnimationUtils.loadAnimation(ctx, R.anim.rotate_down)

            if (a != null) {

                a.reset()

                if (v != null) {

                    a.fillAfter = true

                    v.clearAnimation()
                    v.startAnimation(a)
                }
            }
        }

        fun rotate_up(ctx: Context, v: View?) {

            val a = AnimationUtils.loadAnimation(ctx, R.anim.rotate_up)

            if (a != null) {

                a.reset()

                if (v != null) {

                    a.fillAfter = true

                    v.clearAnimation()
                    v.startAnimation(a)
                }
            }
        }
    }
}
