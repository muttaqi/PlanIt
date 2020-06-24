package com.planit.mobile

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast

import com.evrencoskun.tableview.TableView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.planit.mobile.Adapters.ScheduleTableAdapter
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.DbHelpers.GroupDbHelper
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.Contracts.GroupContract
import com.planit.mobile.data.DbHelpers.MemberDbHelper

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.LinkedList

import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class EventScheduleActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val TAG = EventScheduleActivity::class.java.simpleName

    private var mEventsDb: SQLiteDatabase? = null
    private var mGroupsDb: SQLiteDatabase? = null
    private var mMembersDb: SQLiteDatabase? = null

    private var Jobs: List<String> = ArrayList()
    private val TimeSlots = ArrayList<String>()

    private var timeSlotStartString: String? = null
    private var timeSlotStarts: MutableList<String> = ArrayList()

    private var timeSlotEndString: String? = null
    private var timeSlotEnds: MutableList<String> = ArrayList()

    private var jobString: String? = null
    private var jobNames: MutableList<String> = ArrayList()
    private val jobs = ArrayList<Job>()

    //this
    private var allQuals: MutableList<String>? = null
    private var allPrefs: MutableList<String>? = null
    //becomes this
    private var jobQuals: List<String>? = null
    private var jobPrefs: List<String>? = null
    //which becomes this
    /*private List<List<String>> allJobQuals = new ArrayList<List<String>>();
    private List<List<String>> allJobPrefs = new ArrayList<List<String>>();*/

    private val numQuals = ArrayList<Int>()

    private var eventID: Int = 0
    private var groupID: Int = 0
    private var eventCursor: Cursor? = null
    private var groupCursor: Cursor? = null

    private var eventName: String? = null

    private var mScheduleTable: TableView? = null

    internal var members: MutableList<Member> = ArrayList()
    internal var finalMembers: MutableList<List<String>> = ArrayList()

    internal var tempList: MutableList<MutableList<String>> = ArrayList()
    //list.get(member)(specific unavailability)[specific item*] *(event id : [0], etc.)

    private var mAdView: AdView? = null

    internal lateinit var mCredential: GoogleAccountCredential

    internal var noQualJob = Job("")

    private val data = ArrayList<List<Any>>()

    private val isGooglePlayServicesAvailable: Boolean
        get() {

            Log.d(TAG, "DEBUG ESA isGooglePlayServicesAvailable")
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }
    private val isDeviceOnline: Boolean
        get() {

            Log.d(TAG, "DEBUG ESA isDeviceOnline")
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo !=
                    null && networkInfo.isConnected
        }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {


    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {


    }

    class Job {

        var quals: List<String>? = ArrayList()
        var prefs: List<String>? = ArrayList()

        var id: Int = 0
        var name: String? = null

        constructor(name: String) {

            this.name = name
        }

        constructor(j: Job) {

            this.id = j.id
            this.name = j.name
            this.prefs = j.prefs
            this.quals = j.quals
        }
    }

    class Member(name: String) {

        //stores unavailabilities based on timeslot ids
        var unavailability: List<Int> = ArrayList()

        var name: String
        var id = 0

        var quals: List<String> = ArrayList()
        var prefs: List<String> = ArrayList()

        //updates as schedule is being created (set to false when member is put on a shift, resets after every timeslot)
        var isAvailable = true
        //keeps track of total shifts worked in event (IMPLEMENT HOUR CALCULATION IN FUTURE!!!)
        var business = 0

        init {

            this.name = name
        }
    }

    class sortJobsByNumQualsDescending : Comparator<Job> {
        override fun compare(j1: Job, j2: Job): Int {
            return j2.quals!!.size - j1.quals!!.size
        }
    }

    class sortMembersByBusinessAscending : Comparator<Member> {
        override fun compare(m1: Member, m2: Member): Int {
            return m1.business - m2.business
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_schedule)

        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())

        mAdView = findViewById(R.id.es_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        val i = intent

        eventID = i.getIntExtra("eventID", 0)
        groupID = i.getIntExtra("groupID", 0)

        val eventDbHelper = EventDbHelper(this)
        val groupDbHelper = GroupDbHelper(this)
        val memberDbHelper = MemberDbHelper(this)

        mEventsDb = eventDbHelper.writableDatabase
        mGroupsDb = groupDbHelper.readableDatabase
        mMembersDb = memberDbHelper.readableDatabase

        eventCursor = getEvent(eventID)
        groupCursor = getGroup(groupID)

        eventCursor!!.moveToFirst()
        groupCursor!!.moveToFirst()

        eventName = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_NAME))
        val groupName = groupCursor!!.getString(groupCursor!!.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME))

        val memberCursor = getMembers(groupName)

        memberCursor.moveToFirst()

        var l = 0

        //this shit too complicated to comment; essentially event handles members, members have unavailabilities, unavailabilities hold event id and timeslot id

        do {
            val m = Member(memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_NAME)))

            m.id = memberCursor.getInt(memberCursor.getColumnIndex(MemberContract.MemberEntry._ID))

            val memberUnavailability = ArrayList<Int>()

            m.quals = LinkedList(Arrays.asList(*memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS)).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            m.prefs = LinkedList(Arrays.asList(*memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES)).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            val avail = memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY))
            if (!(avail == null || avail == "")) {

                tempList.add(l, LinkedList(Arrays.asList(*avail.split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())))
                var j = 0
                while (j < tempList[l].size) {

                    val sAvail = tempList[l][j].split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    if (sAvail.size > 1) {

                        if (sAvail[0] == eventID.toString()) {

                            memberUnavailability.add(Integer.valueOf(sAvail[1]))
                            tempList[l].removeAt(j)
                        } else {

                            j++
                        }
                    } else {

                        j++
                    }
                }
            } else {

                tempList.add(l, ArrayList())
                tempList[l].add("")
            }

            m.unavailability = memberUnavailability

            members.add(m)

            l++
        } while (memberCursor.moveToNext())

        mScheduleTable = findViewById<TableView>(R.id.es_table_schedule)

        val adapter = ScheduleTableAdapter(this)

        mScheduleTable!!.setAdapter(adapter)

        getTimeSlots()

        for (j in timeSlotStarts.indices) {

            TimeSlots.add(timeSlotStarts[j] + "-" + timeSlotEnds[j])
        }

        jobString = eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS))
        jobNames = LinkedList(Arrays.asList(*jobString!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        for (j in jobNames.indices) {

            if (jobNames[j] == "") {

                jobNames.removeAt(j)
            }
        }

        Jobs = jobNames

        allQuals = LinkedList(Arrays.asList(*eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        allPrefs = LinkedList(Arrays.asList(*eventCursor!!.getString(eventCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES)).split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        for (j in jobNames.indices) {

            val job = Job(jobNames[j])
            job.id = j

            getJobQualsAndPrefs(j)
            job.quals = jobQuals
            job.prefs = jobPrefs

            jobs.add(job)
        }

        //Creation of actual schedule; members placed in a two dimensional array list based on their positioning in the lost

        val memberArray = Array<Array<String>>(timeSlotStarts.size) { arrayOfEmpties(jobs.size) }

        Collections.sort(jobs, sortJobsByNumQualsDescending())

        for (t in timeSlotStarts.indices) {

            //reset memberAvailable to be true
            for (m in members) {

                m.isAvailable = true
            }

            for (j in jobs.indices) {

                //sort to minimize overworking
                Collections.sort(members, sortMembersByBusinessAscending())

                var done = false

                for (m in members.indices) {

                    //p is used to check if anyone has all preferences and then all - 1, etc.
                    var p = jobs[j].prefs!!.size
                    while (p >= 0) {

                        if (!done) {

                            var unavailable = false

                            //check timeslot availability
                            for (tsID in members[m].unavailability) {

                                if (tsID == t) {

                                    unavailable = true
                                }
                            }

                            //then check qualification and ability
                            val hasQual = ArrayList<Boolean>()
                            for (jQ in jobs[j].quals!!) {

                                if (!(jQ == "_" || jQ == null || jQ == "")) {

                                    hasQual.add(false)
                                    for (mQ in members[m].quals) {

                                        if (mQ.toLowerCase() == jQ.toLowerCase()) {

                                            hasQual[hasQual.size - 1] = true
                                        }
                                    }
                                }
                            }

                            for (q in hasQual.indices) {

                                val b = hasQual[q]
                                if (!b) {

                                    unavailable = true

                                    if (noQualJob.name == "") {

                                        noQualJob = Job(jobs[j])

                                        val theQual = ArrayList<String>()
                                        theQual.add(jobs[j].quals!![q])
                                        noQualJob.quals = theQual
                                    }
                                }
                            }

                            //then preferences
                            var prefCount = 0
                            for (jP in jobs[j].prefs!!) {

                                if (!(jP == "_" || jP == null || jP == "")) {

                                    for (mP in members[m].prefs) {

                                        if (mP == jP) {

                                            prefCount++
                                        }
                                    }
                                }
                            }

                            if (prefCount < p) {

                                unavailable = true
                            }

                            if (!unavailable && members[m].isAvailable) {

                                memberArray[t][j] = members[m].name
                                val mb = members[m].business
                                members[m].business = mb + 1
                                members[m].isAvailable = false

                                done = true
                            }
                        }
                        p -= 1
                    }
                }
            }
        }

        finalMembers = ArrayList()

        for (j in timeSlotStarts.indices) {

            val list = ArrayList<String>()

            for (k in jobs.indices) {

                list.add(memberArray[j][k])
            }

            finalMembers.add(list)
        }

        adapter.setAllItems(Jobs, TimeSlots, finalMembers)

        var done = false
        for (j in finalMembers.indices) {

            for (k in 0 until finalMembers[j].size) {

                if (finalMembers[j][k] == null || finalMembers[j][k] == "") {

                    val ad: AlertDialog
                    val adb = AlertDialog.Builder(this)

                    val tv = TextView(this)

                    if (noQualJob.name != "") {

                        tv.text = "You don't have enough members with the qualification '" + noQualJob.quals!![0] + "'!"
                    } else {

                        tv.text = "You don't have enough members to handle this event!"
                    }
                    tv.setPadding(50, 0, 15, 0)

                    adb.setView(tv).setTitle("Error").setPositiveButton("CLOSE") { dialogInterface, i -> }

                    ad = adb.create()

                    ad.setOnShowListener { ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary)) }

                    ad.show()

                    done = true
                    break
                }

                if (done) {

                    break
                }
            }

            if (done) {

                break
            }
        }
    }

    private fun arrayOfEmpties(size: Int): Array<String> {

        val ret = Array<String>(size, {""})

        return ret
    }

    fun exportToSheets() {

        data.clear()
        val row1 = ArrayList<Any>()

        row1.add("")
        for (s in Jobs) {

            row1.add(s)
        }
        data.add(row1)

        for (j in TimeSlots.indices) {


            val row2 = ArrayList<Any>()
            row2.add(TimeSlots[j])
            for (s in finalMembers[j]) {

                row2.add(s)
            }

            data.add(row2)
        }

        accessSheetsFromApi()

        /*new GoogleAuthorizeUtil.RetrieveSheetsCredentialsTask(this, data, eventName).execute();

        sheetsService = SheetsServiceUtil.getSheetsService(this);

        Log.d(TAG, "DEBUG ESA 638");

        Spreadsheet spreadSheet = new Spreadsheet().setProperties(
                new SpreadsheetProperties().setTitle(eventName + " Schedule " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().getTime())));
        Spreadsheet result = sheetsService
                .spreadsheets()
                .create(spreadSheet).execute();

        Log.d(TAG, "DEBUG ESA 632 the sheets id is not null: " + !result.getSpreadsheetId().equals(null));

        ValueRange body = new ValueRange()
                .setValues(data);
        UpdateValuesResponse response = sheetsService.spreadsheets().values()
                .update(SPREADSHEET_ID, "A1", body)
                .setValueInputOption("RAW")
                .execute();*/
    }

    private fun accessSheetsFromApi() {

        Log.d(TAG, "DEBUG ESA accessSheetsFromApi")

        if (!isGooglePlayServicesAvailable) {
            acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show()
        } else {
            SheetsRequestTask(mCredential).execute()
        }
    }

    private fun acquireGooglePlayServices() {

        Log.d(TAG, "DEBUG ESA acquireGooglePlayServices")
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {

        Log.d(TAG, "DEBUG ESA showGooglePlayServicesAvailabilityErrorDialog")

        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this@EventScheduleActivity,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {

        Log.d(TAG, "DEBUG ESA chooseAccount")
        if (EasyPermissions.hasPermissions(
                        this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential.selectedAccountName = accountName
                accessSheetsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        Log.d(TAG, "DEBUG ESA onRequestPermissionsResult")
        accessSheetsFromApi()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d(TAG, "DEBUG ESA onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this@EventScheduleActivity, "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show()
            } else {
                accessSheetsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                    data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential.selectedAccountName = accountName
                    accessSheetsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                accessSheetsFromApi()
            }
        }
    }

    private inner class SheetsRequestTask internal constructor(credential: GoogleAccountCredential) : AsyncTask<Void, Void, Boolean>() {
        private var mService: com.google.api.services.sheets.v4.Sheets? = null
        private var mLastError: Exception? = null

        init {

            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build()
        }

        @Throws(IOException::class)
        private fun createAndOpenSheets(): Boolean {

            val spreadSheet = Spreadsheet().setProperties(
                    SpreadsheetProperties().setTitle(eventName + " Schedule " + SimpleDateFormat("yyyy/MM/dd HH:mm").format(Calendar.getInstance().time)))

            val result = mService!!
                    .spreadsheets()
                    .create(spreadSheet).execute()

            val spreadsheetId = result.spreadsheetId

            val body = ValueRange()
                    .setValues(data)

            val response = mService!!.spreadsheets().values()
                    .update(spreadsheetId, "A1", body)
                    .setValueInputOption("RAW")
                    .execute()

            return !response.isEmpty()
        }

        override fun onPostExecute(aBoolean: Boolean) {

            if (aBoolean) {

                Toast.makeText(this@EventScheduleActivity, "Sheets successfully created in your Google Drive", Toast.LENGTH_LONG).show()
            } else {

                Toast.makeText(this@EventScheduleActivity, "Sheets was empty", Toast.LENGTH_LONG).show()
            }
        }

        override fun doInBackground(vararg voids: Void): Boolean? {

            try {

                return createAndOpenSheets()
            } catch (e: Exception) {

                mLastError = e
                cancel(true)

                return false
            }

        }

        override fun onCancelled() {

            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            EventScheduleActivity.REQUEST_AUTHORIZATION)
                } else {

                    Log.d(TAG, "DEBUG ESA 846 " + mLastError!!)
                    Toast.makeText(this@EventScheduleActivity, "An error occured", Toast.LENGTH_LONG).show()
                }
            } else {

                Toast.makeText(this@EventScheduleActivity, "Request cancelled", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val mi = menuInflater
        mi.inflate(R.menu.back_export_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.back_button -> {

                val i = Intent(this@EventScheduleActivity, EventDetailsActivity::class.java)
                i.putExtra("eventID", eventID)
                i.putExtra("groupID", groupID)

                startActivity(i)
            }

            R.id.export_button ->

                exportToSheets()
        }

        return super.onOptionsItemSelected(item)
    }

    fun getEvent(eventID: Int): Cursor {

        return mEventsDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry._ID + " = " + eventID.toString(), null, null, null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        )
    }

    fun getGroup(groupID: Int): Cursor {

        return mGroupsDb!!.query(
                GroupContract.GroupEntry.TABLE_NAME, null,
                GroupContract.GroupEntry._ID + " = " + groupID.toString(), null, null, null, null
        )


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

    private fun getMembers(groupName: String): Cursor {

        return mMembersDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID, null, null, null,
                MemberContract.MemberEntry.COLUMN_NAME
        )
    }

    fun getJobQualsAndPrefs(jobID: Int) {

        do {

            if (jobID >= allQuals!!.size || allQuals!!.size == 0) {

                allQuals!!.add("_")
            }

            if (jobID >= allPrefs!!.size || allQuals!!.size == 0) {

                allPrefs!!.add("_")
            }

        } while (jobID >= allQuals!!.size || jobID >= allPrefs!!.size || allQuals!!.size == 0 || allQuals!!.size == 0)

        jobQuals = LinkedList(Arrays.asList(*allQuals!![jobID].split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        jobPrefs = LinkedList(Arrays.asList(*allPrefs!![jobID].split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
    }

    companion object {

        internal val REQUEST_ACCOUNT_PICKER = 1000
        internal val REQUEST_AUTHORIZATION = 1001
        internal val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

        private val PREF_ACCOUNT_NAME = "accountName"
        private val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE)
        private val DATA_STORE_DIR = java.io.File(
                System.getProperty("user.home"), "mobile/google-services.json")
    }
}
