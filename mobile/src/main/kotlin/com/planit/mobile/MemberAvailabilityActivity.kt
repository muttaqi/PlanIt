package com.planit.mobile

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.planit.mobile.Adapters.JobAdapter
import com.planit.mobile.Adapters.TimeSlotAdapter
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.DbHelpers.MemberDbHelper
import com.planit.mobile.R

import java.util.ArrayList
import java.util.Arrays

class MemberAvailabilityActivity : AppCompatActivity(), TimeSlotAdapter.TimeSlotAdapterOnClickHandler {

    internal var eventID: Int = 0
    internal var groupID: Int = 0
    internal lateinit var name: String
    internal var memberID: Int = 0

    internal lateinit var mEventsDb: SQLiteDatabase
    internal lateinit var mMemberDb: SQLiteDatabase
    internal var memberCursor: Cursor? = null
    internal lateinit var eventCursor: Cursor

    private var mTimeSlotAdapter: TimeSlotAdapter? = null

    internal lateinit var mTimeSlotList: RecyclerView

    internal lateinit var txtNullTimeSlot: TextView

    internal var memberUnavailability: MutableList<Array<String>> = ArrayList()
    internal var tempList: MutableList<String> = ArrayList()

    internal var TAG = MemberAvailabilityActivity::class.java.simpleName

    private var mAdView: AdView? = null

    val member: Cursor
        get() = mMemberDb.query(
                MemberContract.MemberEntry.TABLE_NAME,
                null,
                MemberContract.MemberEntry._ID + " = " + memberID, null, null, null, null
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_availability)

        mAdView = findViewById(R.id.ma_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        val i = intent

        eventID = i.getIntExtra("eventID", 0)
        groupID = i.getIntExtra("groupID", 0)
        name = i.getStringExtra("name")
        memberID = i.getIntExtra("memberID", 0)

        Log.d(TAG, "DEBUG MAA 78 $memberID")

        val title = findViewById<View>(R.id.ma_tv_timeslot_title) as TextView

        mTimeSlotList = findViewById<View>(R.id.ed_rv_timeSlots) as RecyclerView

        val eventDbHelper = EventDbHelper(this)
        val memberDbHelper = MemberDbHelper(this)

        mEventsDb = eventDbHelper.writableDatabase
        mMemberDb = memberDbHelper.writableDatabase

        eventCursor = getEvent(eventID)
        memberCursor = member

        eventCursor.moveToFirst()

        title.text = name + "'s Availability For " + eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_NAME))

        var avail: String? = ""

        if (memberCursor!!.moveToFirst() && memberCursor != null) {

            avail = memberCursor!!.getString(memberCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY))
        }

        if (!(avail == null || avail == "")) {

            tempList = ArrayList(Arrays.asList(*avail.split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            var j = 0
            while (j < tempList.size) {

                val sAvail = tempList[j].split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (sAvail.size > 1) {

                    if (sAvail[0] == eventID.toString()) {

                        memberUnavailability.add(sAvail)
                        tempList.removeAt(j)

                        j--
                    }
                }
                j++
            }
        } else {

            tempList.add(0, "")
        }

        val timeSlotLayoutManager = TimeSlotLinearLayoutManager(this)

        mTimeSlotList = findViewById<View>(R.id.ma_rv_timeSlots) as RecyclerView

        mTimeSlotList.layoutManager = timeSlotLayoutManager

        mTimeSlotAdapter = TimeSlotAdapter(this, eventCursor, avail, this, true)

        txtNullTimeSlot = findViewById<View>(R.id.ma_tv_timeslot_null) as TextView
        updateNullView(mTimeSlotAdapter!!, txtNullTimeSlot)

        mTimeSlotList.adapter = mTimeSlotAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.save_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.save_button -> {

                val i = Intent(this@MemberAvailabilityActivity, MemberDetailsActivity::class.java)
                i.putExtra("groupID", groupID)
                i.putExtra("name", name)
                i.putExtra("memberID", memberID)
                i.putExtra("created", true)

                startActivity(i)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(timeSlotStart: String, timeSlotEnd: String, timeSlotID: Int, view: View, checkBox: CheckBox) {

        if (view.id != checkBox.id) {
            checkBox.toggle()
        }

        var ma = ""

        if (checkBox.isChecked) {

            for (i in memberUnavailability.indices) {

                if (memberUnavailability[i][1] == timeSlotID.toString()) {

                    memberUnavailability.removeAt(i)
                }
            }

            for (s in tempList) {

                ma += "$s//"
            }

            for (sa in memberUnavailability) {

                ma += sa[0] + " " + sa[1] + "//"
            }
        } else {

            //concat all info

            for (s in tempList) {

                ma += "$s//"
            }

            for (sa in memberUnavailability) {

                ma += sa[0] + " " + sa[1] + "//"
            }

            ma += "$eventID $timeSlotID"

            memberUnavailability.add(arrayOf(eventID.toString(), timeSlotID.toString()))
        }

        val cv = ContentValues()
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, ma)

        mMemberDb.update(
                MemberContract.MemberEntry.TABLE_NAME,
                cv,
                MemberContract.MemberEntry._ID + " = " + memberID, null
        )
    }

    private class TimeSlotLinearLayoutManager : LinearLayoutManager {
        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }

        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

        constructor(context: Context) : super(context) {}
    }

    fun getEvent(eventID: Int): Cursor {

        return mEventsDb.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry._ID + " = " + eventID.toString(), null, null, null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        )
    }

    fun updateNullView(adapter: RecyclerView.Adapter<*>, textView: TextView) {

        if (adapter.itemCount == 0) {

            textView.visibility = View.VISIBLE
        } else {

            textView.visibility = View.GONE
        }
    }
}
