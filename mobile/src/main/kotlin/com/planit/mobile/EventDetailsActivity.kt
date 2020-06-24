package com.planit.mobile

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteDatabase
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast

import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.planit.mobile.Adapters.JobAdapter
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.Contracts.GroupContract
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.Useful
import com.planit.mobile.Adapters.TimeSlotAdapter
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.DbHelpers.GroupDbHelper
import com.planit.mobile.data.DbHelpers.MemberDbHelper

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.LinkedList
import java.util.TimeZone

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class EventDetailsActivity : AppCompatActivity(), TimeSlotAdapter.TimeSlotAdapterOnClickHandler, JobAdapter.JobAdapterOnClickHandler {
    internal var eventName: String = ""
    internal var eventDate: String = ""
    internal var groupID: Int = 0
    internal var groupName: String = ""
    internal var eventID: Int = 0

    private val TAG = EventDetailsActivity::class.java.simpleName

    private var mEventsDb: SQLiteDatabase? = null
    private var mGroupsDb: SQLiteDatabase? = null
    private var mMembersDb: SQLiteDatabase? = null

    private var mTimeSlotAdapter: TimeSlotAdapter? = null
    private var mJobAdapter: JobAdapter? = null

    private var mTimeSlotList: RecyclerView? = null
    private var mJobList: RecyclerView? = null

    private var eventCursor: Cursor? = null
    private var memberCursor: Cursor? = null

    private var timeSlotStartString: String? = null
    private var timeSlotStarts: MutableList<String> = ArrayList()

    private var timeSlotEndString: String? = null
    private var timeSlotEnds: MutableList<String> = ArrayList()

    private var famEDA: FloatingActionMenu? = null

    private var tvEventName: TextView? = null
    private var tvEventDate: TextView? = null

    private var txtNullTimeslot: TextView? = null
    private var txtNullJob: TextView? = null

    internal lateinit var dateFormat: DateFormat

    internal var newEvent: Boolean = false
    private val memberUnavailability = ArrayList<MutableList<Array<String>>>()
    private val tempList = ArrayList<MutableList<String>>()
    private val memberIDs = ArrayList<Int>()

    internal var hasMembers = true

    internal var day: Int = 0
    internal var month: Int = 0
    internal var year: Int = 0

    internal var startHours = 0
    internal var startMinutes = 0

    internal lateinit var timeDialogEndEdit: TimePickerFragmentEndEdit
    internal lateinit var timeDialogEndNew: TimePickerFragmentEndNew

    internal lateinit var tsaoch: TimeSlotAdapter.TimeSlotAdapterOnClickHandler

    private var mAdView: AdView? = null

    private var mCredential: GoogleAccountCredential? = null

    internal var menu: Menu? = null

    private var calendarEventId: String? = null

    private val currentDate: String? = null

    private var calendarId: String = ""
    internal val HTTP_TRANSPORT: NetHttpTransport = com.google.api.client.http.javanet.NetHttpTransport()
    private var service: Calendar? = null
    private lateinit var event: Event

    private val isGooglePlayServicesAvailable: Boolean
        get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)

            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    private val isDeviceOnline: Boolean
        get() {
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, SCOPES)
                .setBackOff(ExponentialBackOff())

        mAdView = findViewById(R.id.ed_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        dateFormat = SimpleDateFormat("yyyy/MM/dd")

        val intent = intent

        eventID = intent.getIntExtra("eventID", 0)
        groupID = intent.getIntExtra("groupID", 0)
        newEvent = intent.getBooleanExtra("isNewEvent", false)

        mTimeSlotList = findViewById<View>(R.id.ed_rv_timeSlots) as RecyclerView
        mJobList = findViewById<View>(R.id.ed_rv_jobs) as RecyclerView

        val timeSlotLayoutManager = TimeSlotLinearLayoutManager(this)
        val jobLayoutManager = LinearLayoutManager(this)

        mTimeSlotList!!.layoutManager = timeSlotLayoutManager
        mJobList!!.layoutManager = jobLayoutManager

        val eventDbHelper = EventDbHelper(this)
        val groupDbHelper = GroupDbHelper(this)
        val memberDbHelper = MemberDbHelper(this)

        mEventsDb = eventDbHelper.writableDatabase
        mGroupsDb = groupDbHelper.readableDatabase
        mMembersDb = memberDbHelper.writableDatabase

        tvEventName = findViewById<View>(R.id.ed_tv_event_name) as TextView
        tvEventDate = findViewById<View>(R.id.ed_tv_event_date) as TextView

        eventCursor = getEvent(eventID)

        groupName = getGroup(groupID)

        if (!newEvent) {

            eventCursor!!.moveToFirst()

            eventDate = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_DATE))

            eventName = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_NAME))

            calendarEventId = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_CALENDAR_ID))

            tvEventDate!!.text = eventDate
            tvEventName!!.text = eventName

            getTimeSlots()
        } else {

            eventName = intent.getStringExtra("eventName")

            defaultEvent()

            tvEventName!!.text = eventName

            tvEventDate!!.text = eventDate
        }

        mTimeSlotList!!.setHasFixedSize(true)
        mJobList!!.setHasFixedSize(true)

        tsaoch = this
        mTimeSlotAdapter = TimeSlotAdapter(this, eventCursor!!, null, tsaoch, false)
        mJobAdapter = JobAdapter(this, eventCursor, this)

        mTimeSlotList!!.adapter = mTimeSlotAdapter
        mJobList!!.adapter = mJobAdapter

        txtNullTimeslot = findViewById<View>(R.id.ed_tv_timeslot_null) as TextView
        txtNullJob = findViewById<View>(R.id.ed_tv_job_null) as TextView

        updateNullView(mTimeSlotAdapter!!, txtNullTimeslot)
        updateNullView(mJobAdapter!!, txtNullJob)

        famEDA = findViewById<View>(R.id.ed_fam) as FloatingActionMenu

        findViewById<View>(R.id.ed_bt_create_schedule).setOnClickListener {
            if (hasMembers && mTimeSlotAdapter!!.itemCount > 0 && mJobAdapter!!.itemCount > 0) {

                val i = Intent(this@EventDetailsActivity, EventScheduleActivity::class.java)

                i.putExtra("groupID", groupID)
                i.putExtra("eventID", eventID)

                startActivity(i)
            } else if (!hasMembers) {

                Toast.makeText(this@EventDetailsActivity, "Your group needs a member", Toast.LENGTH_SHORT).show()
            } else if (mTimeSlotAdapter!!.itemCount == 0) {

                Toast.makeText(this@EventDetailsActivity, "Your event needs a timeslot", Toast.LENGTH_SHORT).show()
            } else if (mJobAdapter!!.itemCount == 0) {

                Toast.makeText(this@EventDetailsActivity, "Your event needs a job", Toast.LENGTH_SHORT).show()
            }
        }

        //get member info [to handle time slot deletion and delete relevant inavailabilities
        try {
            memberCursor = getMembers(groupName)

            memberCursor!!.moveToFirst()

            var l = 0

            //essentially event handles members, members have unavailabilities, unavailabilities hold event id and timeslot id
            //templist keeps event id and timeslot id as a string instead of splitting into array

            do {

                memberUnavailability.add(l, ArrayList())

                memberIDs.add(memberCursor!!.getInt(memberCursor!!.getColumnIndex(MemberContract.MemberEntry._ID)))

                val avail = memberCursor!!.getString(memberCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY))
                if (!(avail == null || avail == "")) {

                    tempList.add(l, LinkedList(Arrays.asList(*avail.split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())))

                    for (j in 0 until tempList[l].size) {

                        val sAvail = tempList[l][j].split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        //should be bypassed
                        if (sAvail.size > 1) {

                            if (sAvail[0] == eventID.toString()) {

                                memberUnavailability[l].add(sAvail)
                                tempList[l].removeAt(j)
                            }
                        }
                    }

                } else {

                    tempList.add(l, ArrayList())
                    tempList[l].add("")
                }

                l++
            } while (memberCursor!!.moveToNext())
        } catch (ciobe: CursorIndexOutOfBoundsException) {

            hasMembers = false

            ciobe.printStackTrace()
        }

        tvEventName!!.setOnClickListener { nameOnClick() }

        findViewById<View>(R.id.ed_iv_name).setOnClickListener { nameOnClick() }

        tvEventName!!.paintFlags = tvEventName!!.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()

        tvEventDate!!.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this@EventDetailsActivity)

            val cView = layoutInflater.inflate(R.layout.calendar, null) as CalendarView

            alertDialogBuilder.setView(cView)

            val dateVals = LinkedList(Arrays.asList(*eventDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            if (dateVals[1].length == 1) {

                dateVals[1] = "0" + dateVals[1]
            }

            if (dateVals[0].length == 1) {

                dateVals[0] = "0" + dateVals[0]
            }

            val formatter = SimpleDateFormat("yyyy/MM/dd")
            formatter.isLenient = false

            val dateMilis: Long

            val date = dateVals[2] + "/" + dateVals[1] + "/" + dateVals[0]

            try {

                dateMilis = formatter.parse(date).time
                cView.date = dateMilis
            } catch (e: java.text.ParseException) {

                e.printStackTrace()
            }

            cView.setOnDateChangeListener { calendarView, calYear, calMonth, calDayOfMonth ->
                day = calDayOfMonth
                year = calYear
                month = calMonth + 1
            }

            alertDialogBuilder
                    .setNegativeButton("CANCEL") { dialogInterface, i -> famEDA!!.close(true) }
                    .setPositiveButton("SET DATE") { dialogInterface, i ->
                        var sDay = day.toString()
                        var sMonth = month.toString()

                        val dateVals = LinkedList(Arrays.asList(*eventDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

                        if (sDay.length == 1) {

                            sDay = "0$sDay"
                        }

                        if (sMonth.length == 1) {

                            sMonth = "0$sMonth"
                        }

                        if (sMonth == "00") {

                            sMonth = dateVals[0]
                        }

                        if (sDay == "00") {

                            sDay = dateVals[0]
                        }

                        if (sMonth == "00") {

                            sMonth = dateVals[1]
                        }

                        if (year == 0) {

                            year = Integer.valueOf(dateVals[2])
                        }

                        val date = "$sDay/$sMonth/$year"

                        val cv = ContentValues()

                        cv.put(EventContract.EventEntry.COLUMN_EVENT_DATE, date)

                        mEventsDb!!.update(
                                EventContract.EventEntry.TABLE_NAME,
                                cv,
                                EventContract.EventEntry._ID + "= " + eventID.toString(), null
                        )

                        eventCursor = getEvent(eventID)

                        eventCursor!!.moveToFirst()

                        eventDate = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_DATE))

                        tvEventDate!!.text = eventDate

                        famEDA!!.close(true)
                    }

                    .setCustomTitle(findViewById(R.id.calendarTitle))

            val alertDialog = alertDialogBuilder.create()
            alertDialog.setOnShowListener {
                val btnPos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)

                btnPos.setTextColor(resources.getColor(R.color.colorPrimary))
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            }

            alertDialog.show()
        }

        findViewById<View>(R.id.ed_fab_add_timeslot).setOnClickListener { view -> addTimeslot(view) }

        findViewById<View>(R.id.ed_fab_add_job).setOnClickListener {
            val jobNAD = Useful.NameAlertDialog("Job", "CREATE", "CANCEL", "", EditText(this@EventDetailsActivity), this@EventDetailsActivity)
            jobNAD.create()
            jobNAD.ad.setOnShowListener {
                val btnPos = jobNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE)

                jobNAD.setBtnColor()

                btnPos.setOnClickListener {
                    if (jobNAD.et.text.toString() == "" || jobNAD.et.text.toString() == null) {

                        val t = Toast.makeText(applicationContext, "Please enter an job name", Toast.LENGTH_LONG)

                        t.show()
                    } else {

                        val intent = Intent(this@EventDetailsActivity, JobDetailsActivity::class.java)

                        intent.putExtra("eventName", eventName)
                        intent.putExtra("jobName", jobNAD.et.text.toString())
                        intent.putExtra("jobID", eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size)
                        intent.putExtra("groupID", groupID)
                        intent.putExtra("eventID", eventID)

                        startActivity(intent)

                        famEDA!!.close(true)
                    }
                }
            }

            jobNAD.ad.show()
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.save_delete_updatecalendar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.delete_button) {

            val ad = eventDeletePrompt()
            ad.show()
        } else if (item.itemId == R.id.save_button) {
            if (eventCursor != null && eventCursor!!.moveToFirst()) {

                val cv = ContentValues()

                eventName = tvEventName!!.text.toString()

                cv.put(EventContract.EventEntry.COLUMN_EVENT_NAME, eventName)
                storeTimeSlots()

                if (calendarEventId != null) {

                    cv.put(EventContract.EventEntry.COLUMN_CALENDAR_ID, calendarEventId)
                }

                mEventsDb!!.update(
                        EventContract.EventEntry.TABLE_NAME,
                        cv,
                        EventContract.EventEntry._ID + " = " + eventID, null)


                val i = Intent(this@EventDetailsActivity, MainActivity::class.java)

                startActivity(i)
            }
        } else if (item.itemId == R.id.updatecalendar_button) {

            try {

                addOrUpdateEventToCalendar()
            } catch (e: Exception) {
                Log.d(TAG, "DEBUG EDA 667 $e")
                Toast.makeText(this, "An error occured", Toast.LENGTH_LONG).show()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(timeSlotStart: String, timeSlotEnd: String, timeSlotID: Int, view: View, checkBox: CheckBox) {

        if (view.id == R.id.ib_item_timeslot) {

            //remove inavailabilities

            for (i in memberUnavailability.indices) {

                for (j in 0 until memberUnavailability[i].size) {

                    val sa = memberUnavailability[i][j]

                    if (sa[1] == timeSlotID.toString()) {

                        memberUnavailability[i].removeAt(j)
                    }

                    if (Integer.valueOf(sa[1]) > timeSlotID) {

                        val saInt = Integer.valueOf(sa[1]) - 1
                        memberUnavailability[i].set(j, arrayOf(sa[0], saInt.toString()))
                    }
                }
            }

            Toast.makeText(this@EventDetailsActivity, "Removed " + timeSlotStarts[timeSlotID] + " - " + timeSlotEnds[timeSlotID], Toast.LENGTH_SHORT).show()

            timeSlotStarts.removeAt(timeSlotID)
            timeSlotEnds.removeAt(timeSlotID)

            storeTimeSlots()

            eventCursor = getEvent(eventID)

            eventCursor!!.moveToFirst()

            mTimeSlotAdapter = TimeSlotAdapter(this@EventDetailsActivity, eventCursor!!, null, this@EventDetailsActivity, false)

            mTimeSlotList!!.adapter = mTimeSlotAdapter

            mTimeSlotAdapter!!.notifyItemRangeChanged(timeSlotID, timeSlotStarts.size)

            updateNullView(mTimeSlotAdapter!!, txtNullTimeslot)

            Log.d(TAG, "DEBUG EDA 655 $timeSlotStarts")
        } else {

            val timeDialogStart = TimePickerFragmentStartEdit()
            timeDialogStart.show(fragmentManager, "Time Picker")
        }
    }

    override fun onClick(job: String, jobID: Int) {

        val i = Intent(this@EventDetailsActivity, JobDetailsActivity::class.java)

        i.putExtra("jobName", job)
        i.putExtra("jobID", jobID)
        i.putExtra("eventName", eventName)
        i.putExtra("eventID", eventID)
        i.putExtra("groupID", groupID)

        storeTimeSlots()

        startActivity(i)
    }

    override fun onStop() {

        storeTimeSlots()

        super.onStop()
    }

    fun getTimeSlots() {

        timeSlotStartString = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES))
        timeSlotEndString = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES))

        timeSlotStarts = LinkedList(Arrays.asList(*timeSlotStartString!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        timeSlotEnds = LinkedList(Arrays.asList(*timeSlotEndString!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        var tryAgain = false

        do {

            tryAgain = false

            for (i in 0 until timeSlotStarts.size - 1) {

                if (Integer.valueOf(timeSlotStarts[i].substring(0, 2)) > Integer.valueOf(timeSlotStarts[i + 1].substring(0, 2))) {

                    val a = timeSlotStarts[i]
                    val b = timeSlotStarts[i + 1]

                    val c = timeSlotEnds[i]
                    val d = timeSlotEnds[i + 1]

                    timeSlotStarts[i] = b
                    timeSlotStarts[i + 1] = a

                    timeSlotEnds[i] = d
                    timeSlotEnds[i + 1] = c

                    tryAgain = true
                } else if (Integer.valueOf(timeSlotStarts[i].substring(0, 2)) === Integer.valueOf(timeSlotStarts[i + 1].substring(0, 2)) && Integer.valueOf(timeSlotStarts[i].substring(3, 5)) > Integer.valueOf(timeSlotStarts[i + 1].substring(3, 5))) {

                    val a = timeSlotStarts[i]
                    val b = timeSlotStarts[i + 1]

                    val c = timeSlotEnds[i]
                    val d = timeSlotEnds[i + 1]

                    timeSlotStarts[i] = b
                    timeSlotStarts[i + 1] = a

                    timeSlotEnds[i] = d
                    timeSlotEnds[i + 1] = c

                    tryAgain = true
                }
            }
        } while (tryAgain)
    }

    fun storeTimeSlots(): Int {

        timeSlotStarts.remove("")
        timeSlotEnds.remove("")

        timeSlotStartString = ""
        timeSlotEndString = ""

        var tryAgain = false

        //sort
        do {

            tryAgain = false

            for (i in 0 until timeSlotStarts.size - 1) {

                if (Integer.valueOf(timeSlotStarts[i].substring(0, 2)) > Integer.valueOf(timeSlotStarts[i + 1].substring(0, 2))) {

                    val a = timeSlotStarts[i]
                    val b = timeSlotStarts[i + 1]

                    val c = timeSlotEnds[i]
                    val d = timeSlotEnds[i + 1]

                    timeSlotStarts[i] = b
                    timeSlotStarts[i + 1] = a

                    timeSlotEnds[i] = d
                    timeSlotEnds[i + 1] = c

                    tryAgain = true
                } else if (Integer.valueOf(timeSlotStarts[i].substring(0, 2)) === Integer.valueOf(timeSlotStarts[i + 1].substring(0, 2)) && Integer.valueOf(timeSlotStarts[i].substring(3, 5)) > Integer.valueOf(timeSlotStarts[i + 1].substring(3, 5))) {

                    val a = timeSlotStarts[i]
                    val b = timeSlotStarts[i + 1]

                    val c = timeSlotEnds[i]
                    val d = timeSlotEnds[i + 1]

                    timeSlotStarts[i] = b
                    timeSlotStarts[i + 1] = a

                    timeSlotEnds[i] = d
                    timeSlotEnds[i + 1] = c

                    tryAgain = true
                }
            }
        } while (tryAgain)

        if (timeSlotStarts.size > 0 && timeSlotEnds.size > 0) {
            for (s in timeSlotStarts) {

                timeSlotStartString += "$s||"
            }

            for (s in timeSlotEnds) {

                timeSlotEndString += "$s||"
            }

            timeSlotStartString = timeSlotStartString!!.substring(0, timeSlotStartString!!.length - 2)
            timeSlotEndString = timeSlotEndString!!.substring(0, timeSlotEndString!!.length - 2)
        }

        val cv = ContentValues()

        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES, timeSlotStartString)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES, timeSlotEndString)

        return mEventsDb!!.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry._ID + "= " + eventID.toString(), null
        )
    }

    fun defaultEvent() {

        val cv = ContentValues()

        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val date = Date()
        eventDate = formatter.format(date)

        cv.put(EventContract.EventEntry.COLUMN_EVENT_NAME, eventName)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, "Default Job")
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES, "10:30")
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES, "12:30")
        cv.put(EventContract.EventEntry.COLUMN_EVENT_DATE, eventDate)
        cv.put(EventContract.EventEntry.COLUMN_GROUP_ID, groupID)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, "")
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, "")

        timeSlotStarts.add("10:30")
        timeSlotEnds.add("12:30")

        eventID = mEventsDb!!.insertOrThrow(EventContract.EventEntry.TABLE_NAME, null, cv).toInt()

        eventCursor = mEventsDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry._ID + " = " + eventID, null, null, null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        )

        eventCursor!!.moveToFirst()
    }

    fun getEvent(eventID: Int): Cursor {


        return mEventsDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry._ID + " = " + eventID.toString(), null, null, null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        )
    }

    fun getGroup(groupID: Int): String {

        val groupCursor = mGroupsDb!!.query(
                GroupContract.GroupEntry.TABLE_NAME, null,
                GroupContract.GroupEntry._ID + " = " + groupID.toString(), null, null, null, null
        )

        groupCursor.moveToFirst()
        return groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
    }

    fun eventDeletePrompt(): AlertDialog {

        val adb = AlertDialog.Builder(this)

        adb
                .setNegativeButton("CANCEL") { dialogInterface, i -> }
                .setPositiveButton("CONTINUE") { dialogInterface, i -> deleteEventExitToMain() }
                .setTitle("Are you sure you would like to delete this event?")

        val retPrompt = adb.create()

        retPrompt.setOnShowListener {
            retPrompt.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            retPrompt.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.colorPrimary))
        }

        return retPrompt
    }

    fun deleteEventExitToMain() {

        removeEvent(eventDate)

        val intent = Intent(this@EventDetailsActivity, MainActivity::class.java)
        startActivity(intent)
    }

    fun removeEvent(eventDate: String): Boolean {

        return mEventsDb!!.delete(EventContract.EventEntry.TABLE_NAME, EventContract.EventEntry._ID + "=" + eventID, null) > 0
    }

    private fun getMembers(groupName: String): Cursor {

        return mMembersDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID, null, null, null,
                MemberContract.MemberEntry.COLUMN_NAME
        )
    }

    override fun onPause() {

        super.onPause()

        //concat all info

        for (i in memberIDs.indices) {

            val memberID = memberIDs[i]

            var ma = ""

            for (s in tempList[i]) {

                ma += "$s//"
            }

            for (sa in memberUnavailability[i]) {

                ma += sa[0] + " " + sa[1] + "//"
            }

            val cv = ContentValues()
            cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, ma)

            mMembersDb!!.update(
                    MemberContract.MemberEntry.TABLE_NAME,
                    cv,
                    MemberContract.MemberEntry._ID + " = " + memberID, null
            )
        }
    }

    fun updateNullView(adapter: RecyclerView.Adapter<*>, textView: TextView?) {

        if (adapter.itemCount == 0) {

            textView!!.visibility = View.VISIBLE
        } else {

            textView!!.visibility = View.GONE
        }
    }

    fun addTimeslot(view: View) {

        val v = view

        startHours = 0
        startMinutes = 0

        val timeDialogStart = TimePickerFragmentStartNew()
        timeDialogStart.show(fragmentManager, "Time Picker")
    }

    class TimePickerFragmentStartEdit : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        internal var minute = 0
        internal var hour = 0
        internal lateinit var EDA: EventDetailsActivity

        internal lateinit var tpd: TimePickerDialog

        internal var timeSlotID: Int = 0

        fun setTimes(hr: Int, min: Int) {

            this.hour = hr
            this.minute = min
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

            EDA = activity as EventDetailsActivity

            /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = TimePickerDialog(activity,
                    R.style.Theme_Dialog, this, hour, minute, true)

            // Return the TimePickerDialog
            return tpd
        }

        override fun onTimeSet(view: TimePicker, hours: Int, minutes: Int) {

            EDA.timeDialogEndEdit = TimePickerFragmentEndEdit()
            EDA.timeDialogEndEdit.setTimeSlotID(timeSlotID)
            EDA.timeDialogEndEdit.setStartHour(hours)
            EDA.timeDialogEndEdit.setStartMinute(minutes)

            EDA.timeDialogEndEdit.show(fragmentManager, "Time Picker")
        }
    }

    class TimePickerFragmentEndEdit : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        internal var minute = 0
        internal var hour = 0
        internal lateinit var EDA: EventDetailsActivity

        internal lateinit var tpd: TimePickerDialog

        internal var timeSlotID: Int = 0

        internal var startHour: Int = 0
        internal var startMinute: Int = 0

        fun setTimes(hr: Int, min: Int) {

            this.hour = hr
            this.minute = min
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

            EDA = activity as EventDetailsActivity

            /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = TimePickerDialog(activity,
                    R.style.Theme_Dialog, this, hour, minute, true)

            // Return the TimePickerDialog
            return tpd
        }

        fun setTimeSlotID(timeSlotID: Int) {
            this.timeSlotID = timeSlotID
        }

        fun setStartHour(startHour: Int) {
            this.startHour = startHour
        }

        fun setStartMinute(startMinute: Int) {
            this.startMinute = startMinute
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {

            if (hourOfDay < startHour || hourOfDay == startHour && minute <= startMinute) {

                Toast.makeText(EDA.applicationContext, "The timeslot must end after it starts", Toast.LENGTH_SHORT).show()
            } else {
                //format hour:minute string
                var hoursString = startHour.toString()
                var minutesString = startMinute.toString()

                if (hoursString.length == 1) {

                    hoursString = "0$hoursString"
                }

                if (minutesString.length == 1) {

                    minutesString = "0$minutesString"
                }

                EDA.timeSlotStarts[timeSlotID] = "$hoursString:$minutesString"

                hoursString = hourOfDay.toString()
                minutesString = minute.toString()

                if (hoursString.length == 1) {

                    hoursString = "0$hoursString"
                }

                if (minutesString.length == 1) {

                    minutesString = "0$minutesString"
                }

                //set on list and store
                EDA.timeSlotEnds[timeSlotID] = "$hoursString:$minutesString"

                EDA.storeTimeSlots()

                EDA.eventCursor = EDA.getEvent(EDA.eventID)

                EDA.eventCursor!!.moveToFirst()

                //reset/update adapter and null view
                EDA.mTimeSlotAdapter = TimeSlotAdapter(EDA.applicationContext, EDA.eventCursor!!, null, EDA.tsaoch, false)

                EDA.mTimeSlotList!!.adapter = EDA.mTimeSlotAdapter

                if (EDA.mTimeSlotList != null) {

                    EDA.mTimeSlotList!!.invalidate()
                }

                EDA.mTimeSlotAdapter!!.notifyDataSetChanged()

                EDA.updateNullView(EDA.mTimeSlotAdapter!!, EDA.txtNullTimeslot)
            }
        }
    }

    class TimePickerFragmentStartNew : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        internal var minute = 0
        internal var hour = 0
        internal lateinit var EDA: EventDetailsActivity

        internal lateinit var tpd: TimePickerDialog

        internal var timeSlotID: Int = 0

        fun setTimes(hr: Int, min: Int) {

            this.hour = hr
            this.minute = min
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

            EDA = activity as EventDetailsActivity

            /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = TimePickerDialog(activity,
                    R.style.Theme_Dialog, this, hour, minute, true)

            // Return the TimePickerDialog
            return tpd
        }

        override fun onTimeSet(view: TimePicker, hours: Int, minutes: Int) {

            EDA.timeDialogEndNew = TimePickerFragmentEndNew()
            EDA.timeDialogEndNew.setTimeSlotID(timeSlotID)
            EDA.timeDialogEndNew.setStartHour(hours)
            EDA.timeDialogEndNew.setStartMinute(minutes)

            EDA.timeDialogEndNew.show(fragmentManager, "Time Picker")
        }
    }

    class TimePickerFragmentEndNew : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        internal var minute = 0
        internal var hour = 0
        internal lateinit var EDA: EventDetailsActivity

        internal lateinit var tpd: TimePickerDialog

        internal var timeSlotID: Int = 0

        internal var startHour: Int = 0
        internal var startMinute: Int = 0

        fun setTimes(hr: Int, min: Int) {

            this.hour = hr
            this.minute = min
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

            EDA = activity as EventDetailsActivity

            /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = TimePickerDialog(activity,
                    R.style.Theme_Dialog, this, hour, minute, true)

            // Return the TimePickerDialog
            return tpd
        }

        fun setTimeSlotID(timeSlotID: Int) {
            this.timeSlotID = timeSlotID
        }

        fun setStartHour(startHour: Int) {
            this.startHour = startHour
        }

        fun setStartMinute(startMinute: Int) {
            this.startMinute = startMinute
        }

        override fun onTimeSet(view: TimePicker, hours: Int, minutes: Int) {

            if (hours > startHour || hours == startHour && minutes > startMinute) {

                var startHoursString = startHour.toString()
                var startMinutesString = startMinute.toString()

                if (startHoursString.length == 1) {

                    startHoursString = "0$startHoursString"
                }

                if (startMinutesString.length == 1) {

                    startMinutesString = "0$startMinutesString"
                }

                EDA.timeSlotStarts.add("$startHoursString:$startMinutesString")

                var endHoursString = hours.toString()
                var endMinutesString = minutes.toString()

                if (endHoursString.length == 1) {

                    endHoursString = "0$endHoursString"
                }

                if (endMinutesString.length == 1) {

                    endMinutesString = "0$endMinutesString"
                }

                EDA.timeSlotEnds.add("$endHoursString:$endMinutesString")

                Log.d(EDA.TAG, "DEBUG EDA 1304 " + EDA.timeSlotStarts + " " + EDA.timeSlotEnds)

                EDA.storeTimeSlots()

                EDA.eventCursor = EDA.getEvent(EDA.eventID)
                EDA.eventCursor!!.moveToFirst()

                EDA.mTimeSlotAdapter = TimeSlotAdapter(EDA.applicationContext, EDA.eventCursor!!, null, EDA.tsaoch, false)

                EDA.mTimeSlotList!!.adapter = EDA.mTimeSlotAdapter

                if (EDA.mTimeSlotList != null) {

                    EDA.mTimeSlotList!!.invalidate()
                }

                EDA.mTimeSlotAdapter!!.notifyDataSetChanged()

                EDA.updateNullView(EDA.mTimeSlotAdapter!!, EDA.txtNullTimeslot)

                EDA.famEDA!!.close(true)
            } else {

                Toast.makeText(EDA.applicationContext, "The timeslot must end after it begins", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun nameOnClick() {

        famEDA!!.close(true)

        val eventNAD = Useful.NameAlertDialog("Event", "DONE", "CANCEL", tvEventName!!.text.toString(), EditText(this@EventDetailsActivity), this@EventDetailsActivity)
        eventNAD.create()
        eventNAD.ad.setOnShowListener {
            eventNAD.setBtnColor()
            eventNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                eventNAD.ad.dismiss()

                eventName = eventNAD.et.text.toString()
                tvEventName!!.text = eventName
            }
        }

        eventNAD.ad.show()
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun addOrUpdateEventToCalendar() {

        System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files\\Java\\jre1.8.0_131\\lib\\security\\cacerts")

        calendarId = "primary"

        service = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, mCredential)
                .setApplicationName(APPLICATION_NAME)
                .build()

        event = Event().setSummary(eventName)


        Log.d(TAG, "DEBUG ")
        val current = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ").format(Date())

        Log.d(TAG, "DEBUG EDA 1382 $current")

        val dateArr = eventDate.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val startArr = timeSlotStarts[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val endArr = timeSlotEnds[timeSlotEnds.size - 1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val startDate = DateTime.parseRfc3339(dateArr[2] + "-" + dateArr[1] + "-" + dateArr[0] + "T" + startArr[0] + ":" + startArr[1] + ":00.00" + current.substring(current.length - 5, current.length - 2) + ":" + current.substring(current.length - 2))
        val endDate = DateTime.parseRfc3339(dateArr[2] + "-" + dateArr[1] + "-" + dateArr[0] + "T" + endArr[0] + ":" + endArr[1] + ":00.00" + current.substring(current.length - 5, current.length - 2) + ":" + current.substring(current.length - 2))
        val startEdt = EventDateTime().setDateTime(startDate)
        val endEdt = EventDateTime().setDateTime(endDate)

        event!!.start = startEdt
        event!!.end = endEdt

        // Build a new authorized API client service.


        /* for notifications, FUTURE IMPLEMENTATION
        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList(recurrence));
        event.setAttendees(Arrays.asList(eventAttendees));
        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };*/

        accessCalendarFromApi()
    }

    private inner class CalendarInputTask(internal var service: Calendar?, internal var event: Event, internal var calendarId: String) : AsyncTask<Void, Void, Boolean>() {

        internal var lastError: Exception? = null

        override fun doInBackground(vararg voids: Void): Boolean? {

            try {
                //event.send
                if (service != null) {

                    val events = service!!.events().list(calendarId)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute()
                            .items

                    var isOnCalendar = false

                    for (e in events) {

                        if (e.summary == eventName || e.id == calendarEventId) {

                            isOnCalendar = true

                            calendarEventId = e.id
                        }
                    }

                    if (!isOnCalendar) {
                        calendarEventId = service!!.events().insert(calendarId, event).setSendNotifications(true).execute().id

                        val cv = ContentValues()

                        cv.put(EventContract.EventEntry.COLUMN_CALENDAR_ID, calendarEventId)

                        mEventsDb!!.update(
                                EventContract.EventEntry.TABLE_NAME,
                                cv,
                                EventContract.EventEntry._ID + "= " + eventID.toString(), null
                        )
                    } else {

                        service!!.events().update(calendarId, calendarEventId!!, event).setSendNotifications(true).execute()
                    }

                    return true
                }
            } catch (ue: UserRecoverableAuthIOException) {
                startActivityForResult(ue.intent, REQUEST_AUTHORIZATION)

                return true
            } catch (e: IOException) {
                Log.d(TAG, "DEBUG EDA 1503 $e")
            }

            return false
        }

        override fun onPostExecute(aBoolean: Boolean) {

            if (aBoolean) {

                Toast.makeText(this@EventDetailsActivity, "Event successfully updated on calendar", Toast.LENGTH_LONG).show()
            } else {

                Toast.makeText(this@EventDetailsActivity, "An error occured", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun accessCalendarFromApi() {
        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show()
        } else {

            CalendarInputTask(service, event, calendarId).execute()
        }
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {

        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@EventDetailsActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)

            if (accountName != null) {

                mCredential!!.selectedAccountName = accountName
                accessCalendarFromApi()
            } else {

                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential!!.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this@EventDetailsActivity, "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show()
            } else {
                accessCalendarFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    accessCalendarFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                accessCalendarFromApi()
            }
        }
    }

    companion object {
        private val SCOPES = listOf(CalendarScopes.CALENDAR)
        private val DATA_STORE_DIR = java.io.File(
                System.getProperty("user.home"), "mobile/google-services.json")
        private val PREF_ACCOUNT_NAME = "accountName"

        internal val REQUEST_ACCOUNT_PICKER = 1000
        internal val REQUEST_AUTHORIZATION = 1001
        internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

        private val APPLICATION_NAME = "Google Calendar API Java Quickstart"
        private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
        private val TOKENS_DIRECTORY_PATH = "tokens"

        /**
         * Global instance of the scopes required by this quickstart.
         * If modifying these scopes, delete your previously saved tokens/ folder.
         */
        private val CREDENTIALS_FILE_PATH = "client_id.json"
    }
}
