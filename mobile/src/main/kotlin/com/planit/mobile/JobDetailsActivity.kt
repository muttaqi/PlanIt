package com.planit.mobile

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.planit.mobile.Adapters.PreferenceAdapter
import com.planit.mobile.Adapters.QualificationAdapter
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.DbHelpers.MemberDbHelper
import com.planit.mobile.data.Useful

import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedList

class JobDetailsActivity : AppCompatActivity() {

    private val TAG = JobDetailsActivity::class.java.simpleName

    private var eventName: String? = null
    private var eventID: Int = 0
    private var job: String? = null
    private var jobID: Int = 0
    private var groupID: Int = 0

    private var newJob: Boolean = false

    private var mEventDb: SQLiteDatabase? = null
    private var mMemberDb: SQLiteDatabase? = null

    private var mPrefAdapter: PreferenceAdapter? = null
    private var mQualAdapter: QualificationAdapter? = null

    private var mPrefList: RecyclerView? = null
    private var mQualList: RecyclerView? = null

    private var txtNullPref: TextView? = null
    private var txtNullQual: TextView? = null

    private var mCursor: Cursor? = null

    private var qualCursor: Cursor? = null
    private var prefCursor: Cursor? = null
    private val globalQuals = ArrayList<String>()
    private val globalPrefs = ArrayList<String>()

    private var prefs: MutableList<String> = ArrayList()
    private var quals: MutableList<String> = ArrayList()

    private var allQuals: MutableList<String> = ArrayList()
    private var allPrefs: MutableList<String> = ArrayList()

    private var famJDA: FloatingActionMenu? = null

    private var tvJobName: TextView? = null

    var mContext: Context = this

    private var mAdView: AdView? = null

    val allEvents: Cursor
        get() = mEventDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null, null, null, null, null, null
        )

    val allMembers: Cursor
        get() = mMemberDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null, null, null, null, null, null
        )

    val groupMembers: Cursor
        get() = mMemberDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID, null, null, null, null
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        mAdView = findViewById(R.id.jd_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        val intent = this.intent

        eventName = intent.getStringExtra("eventName")
        eventID = intent.getIntExtra("eventID", 0)

        job = intent.getStringExtra("jobName")
        jobID = intent.getIntExtra("jobID", 0)
        groupID = intent.getIntExtra("groupID", 0)

        newJob = true

        Log.d(TAG, "DEBUG JDA 73 INTENT VALUES $eventName $job")

        tvJobName = findViewById<View>(R.id.jd_tv_job_name) as TextView
        tvJobName!!.text = job

        mPrefList = findViewById<View>(R.id.jd_rv_prefs) as RecyclerView
        mQualList = findViewById<View>(R.id.jd_rv_quals) as RecyclerView

        val prefLayoutManager = LinearLayoutManager(this)
        val qualLayoutManager = LinearLayoutManager(this)

        mPrefList!!.layoutManager = prefLayoutManager
        mQualList!!.layoutManager = qualLayoutManager

        val eventDbHelper = EventDbHelper(this)
        val memberDbHelper = MemberDbHelper(this)

        mEventDb = eventDbHelper.writableDatabase
        mMemberDb = memberDbHelper.writableDatabase

        mCursor = getEventData(eventName)

        if (!mCursor!!.moveToFirst() || mCursor == null) {

            Log.d(TAG, "DEBUG JDA 104 shit makes no sense")
        } else {

            val jobString = mCursor!!.getString(mCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS))
            val jobs = LinkedList(Arrays.asList(*jobString.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            Log.d(TAG, "DEBUG JDA 116 " + job!!)

            for (s in jobs) {

                Log.d(TAG, "DEBUG JDA 114 $s")

                if (s == job) {

                    newJob = false
                }
            }
        }

        mPrefList!!.setHasFixedSize(true)
        mQualList!!.setHasFixedSize(true)

        getQualsAndPrefs(mCursor!!, jobID)

        Log.d(TAG, "DEBUG JDA 104 $quals")

        Useful.removeUnderscores(quals)
        Useful.removeUnderscores(prefs)

        Log.d(TAG, "DEBUG JDA 109 $quals")

        txtNullPref = findViewById<View>(R.id.jd_tv_pref_null) as TextView
        txtNullQual = findViewById<View>(R.id.jd_tv_qual_null) as TextView

        mQualAdapter = QualificationAdapter(this, quals, txtNullQual)
        mPrefAdapter = PreferenceAdapter(this, prefs, txtNullPref)

        mPrefList!!.adapter = mPrefAdapter
        mQualList!!.adapter = mQualAdapter

        updateNullView(mPrefAdapter!!, txtNullPref)
        updateNullView(mQualAdapter!!, txtNullQual)

        qualCursor = allEvents

        //qualCursor.moveToFirst();
        while (qualCursor!!.moveToNext()) {

            val q = qualCursor!!.getString(qualCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS))
            if (q != null || q != "") {

                for (s1 in q!!.split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                    for (s2 in s1.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                        if (!globalQuals.contains(s2)) {

                            globalQuals.add(s2)
                        }
                    }
                }
            }
        }

        qualCursor = allMembers

        //qualCursor.moveToFirst();
        while (qualCursor!!.moveToNext()) {

            val q = qualCursor!!.getString(qualCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS))
            if (q != null || q != "") {

                for (s in q!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                    if (!globalQuals.contains(s)) {

                        globalQuals.add(s)
                    }
                }
            }
        }

        prefCursor = allEvents

        //prefCursor.moveToFirst();
        while (prefCursor!!.moveToNext()) {

            val p = prefCursor!!.getString(prefCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES))
            if (p != null || p != "") {

                for (s1 in p!!.split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                    for (s2 in s1.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                        if (!globalPrefs.contains(s2)) {

                            globalPrefs.add(s2)
                        }
                    }
                }
            }
        }

        prefCursor = allMembers

        //prefCursor.moveToFirst();
        while (prefCursor!!.moveToNext()) {

            val p = prefCursor!!.getString(prefCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES))
            if (p != null || p != "") {

                for (s in p!!.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {

                    if (!globalPrefs.contains(s)) {

                        globalPrefs.add(s)
                    }
                }
            }
        }

        famJDA = findViewById<View>(R.id.jd_fam) as FloatingActionMenu

        findViewById<View>(R.id.jd_fab_add_qual).setOnClickListener {
            var qualsArr = arrayOfNulls<String>(globalQuals.size)
            qualsArr = globalQuals.toTypedArray<String?>()
            val actvAdapter = ArrayAdapter(mContext,
                    R.layout.custom_expandable_list_item, qualsArr)
            val actv = AutoCompleteTextView(this@JobDetailsActivity)
            actv.setAdapter(actvAdapter)

            val qualNAD = Useful.NameAlertDialog("Qualification", "ADD", "CANCEL", "", actv, this@JobDetailsActivity)
            qualNAD.create()
            qualNAD.ad.setOnShowListener {
                qualNAD.setBtnColor()
                qualNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    if (qualNAD.actv.text.toString() == "" || qualNAD.actv.text.toString() == null) {

                        val t = Toast.makeText(this@JobDetailsActivity, "Please enter a qualification name", Toast.LENGTH_LONG)

                        t.show()
                    } else {

                        quals.add(qualNAD.actv.text.toString())

                        storeJobData()

                        mQualAdapter!!.notifyDataSetChanged()

                        updateNullView(mQualAdapter!!, txtNullQual)

                        famJDA!!.close(true)

                        qualNAD.ad.dismiss()
                    }
                }
            }

            qualNAD.ad.show()
        }

        findViewById<View>(R.id.jd_fab_add_pref).setOnClickListener {
            var prefsArr = arrayOfNulls<String>(globalPrefs.size)
            prefsArr = globalPrefs.toTypedArray<String?>()
            val actvAdapter = ArrayAdapter(mContext,
                    R.layout.custom_expandable_list_item, prefsArr)
            val actv = AutoCompleteTextView(this@JobDetailsActivity)
            actv.setAdapter(actvAdapter)

            val prefNAD = Useful.NameAlertDialog("Preference", "ADD", "CANCEL", "", actv, this@JobDetailsActivity)
            prefNAD.create()
            prefNAD.ad.setOnShowListener {
                prefNAD.setBtnColor()
                prefNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    if (prefNAD.actv.text.toString() == "" || prefNAD.actv.text.toString() == null) {

                        val t = Toast.makeText(this@JobDetailsActivity, "Please enter a preference name", Toast.LENGTH_LONG)

                        t.show()
                    } else {

                        prefs.add(prefNAD.actv.text.toString())

                        storeJobData()

                        mPrefAdapter!!.notifyDataSetChanged()

                        updateNullView(mPrefAdapter!!, txtNullPref)

                        famJDA!!.close(true)

                        prefNAD.ad.dismiss()
                    }
                }
            }

            prefNAD.ad.show()
        }

        tvJobName!!.setOnClickListener { nameOnClick() }

        findViewById<View>(R.id.jd_iv_name).setOnClickListener { nameOnClick() }

        tvJobName!!.paintFlags = tvJobName!!.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()

        findViewById<View>(R.id.jd_iv_qual).setOnClickListener {
            val adb = AlertDialog.Builder(mContext)

            val tv = TextView(adb.context)
            tv.text = "Qualifications allow you to control which members are scheduled for certain jobs. " +
                    "Just add a qualification to a job, and add a qualification of the same name to one or more members, " +
                    "and only those members will be scheduled for the job. Be careful though, if no member with the " +
                    "qualification is available, you won't be able to create a schedule!"
            tv.setPadding(24, 16, 0, 0)

            adb.setPositiveButton("CLOSE") { dialogInterface, i -> }.setTitle("Qualifications").setView(tv)

            val ad = adb.create()

            ad.setOnShowListener { ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary)) }

            ad.show()
        }

        findViewById<View>(R.id.jd_iv_pref).setOnClickListener {
            val adb = AlertDialog.Builder(mContext)

            val tv = TextView(adb.context)
            tv.text = "Preferences allow you to influence which members are scheduled for certain jobs. " +
                    "Just add a preference to a job, and add a preference of the same name to one or more members, " +
                    "and those members will be prioritized when scheduling for that job. If no member with the " +
                    "preference is available, the schedule will continue with other members."
            tv.setPadding(24, 16, 0, 0)

            adb.setPositiveButton("CLOSE") { dialogInterface, i -> }.setTitle("Preferences").setView(tv)

            val ad = adb.create()

            ad.setOnShowListener { ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary)) }
            ad.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val menuInflater = menuInflater

        menuInflater.inflate(R.menu.save_delete_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.delete_button) {

            deleteThenStoreJobData()

            val i = Intent(this@JobDetailsActivity, EventDetailsActivity::class.java)

            i.putExtra("eventName", eventName)
            i.putExtra("groupID", groupID)
            i.putExtra("eventID", eventID)

            Log.d(TAG, "DEBUG JDA 173 $eventID")

            startActivity(i)
        } else if (item.itemId == R.id.save_button) {

            storeJobData()

            val i = Intent(this@JobDetailsActivity, EventDetailsActivity::class.java)

            i.putExtra("eventName", eventName)
            i.putExtra("groupID", groupID)
            i.putExtra("eventID", eventID)

            Log.d(TAG, "DEBUG JDA 173 $eventID")

            startActivity(i)
        }

        return super.onOptionsItemSelected(item)
    }

    fun getEventData(eventName: String?): Cursor {

        return mEventDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry._ID + "=" + eventID, null, null, null, null
        )
    }

    fun updateMember(cv: ContentValues, mId: Int) {

        mMemberDb!!.update(
                MemberContract.MemberEntry.TABLE_NAME,
                cv,
                MemberContract.MemberEntry._ID + "=" + mId, null
        )
    }

    fun getQualsAndPrefs(mCursor: Cursor, jobID: Int) {
        var mCursor = mCursor

        allQuals = LinkedList(Arrays.asList(*mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        allPrefs = LinkedList(Arrays.asList(*mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES)).split("//".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        Log.d(TAG, "DEBUG JDA 149 $quals")

        do {

            if (jobID >= allQuals.size || allQuals.size == 0) {

                allQuals.add("")
            }

            if (jobID >= allPrefs.size || allQuals.size == 0) {

                allPrefs.add("")
            }

        } while (jobID >= allQuals.size || jobID >= allPrefs.size || allQuals.size == 0 || allQuals.size == 0)

        mCursor.moveToFirst()

        do {

            Log.d(TAG, "DEBUG JDA 221 " + mCursor.getInt(mCursor.getColumnIndex(EventContract.EventEntry._ID)))
        } while (mCursor.moveToNext())

        mCursor.moveToFirst()

        mCursor = getEventData(eventName)

        Log.d(TAG, "DEBUG JDA 228 id $jobID BUT $allQuals")

        quals = LinkedList(Arrays.asList(*allQuals[jobID].split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        prefs = LinkedList(Arrays.asList(*allPrefs[jobID].split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        Log.d(TAG, "DEBUG JDA 160 $quals")
    }

    fun storeJobData() {

        quals = mQualAdapter!!.quals
        prefs = mPrefAdapter!!.prefs

        Log.d(TAG, "DEBUG JDA 495 allQuals: $allQuals allPrefs: $allPrefs")

        var qualJoined = ""

        Log.d(TAG, "DEBUG JDA 311 $quals")

        for (s in quals) {

            qualJoined += "$s||"
        }

        if (quals.size > 0)
            qualJoined = qualJoined.substring(0, qualJoined.length - 2)

        Log.d(TAG, "DEBUG JDA 324 $qualJoined")

        allQuals[jobID] = qualJoined

        var prefJoined = ""

        for (s in prefs) {

            prefJoined += "$s||"
        }

        if (prefs.size > 0)
            prefJoined = prefJoined.substring(0, prefJoined.length - 2)

        allPrefs[jobID] = prefJoined

        var allQualsJoined = ""

        Log.d(TAG, "DEBUG JDA 494 $allQuals")

        for (s in allQuals) {

            allQualsJoined = "$allQualsJoined$s//"
        }

        if (allQuals.size > 0)
            allQualsJoined = allQualsJoined.substring(0, allQualsJoined.length - 2)

        Log.d(TAG, "DEBUG JDA 374 $allQualsJoined")

        var allPrefsJoined = ""

        for (s in allPrefs) {

            allPrefsJoined += "$s//"
        }

        if (allPrefs.size > 0)
            allPrefsJoined = allPrefsJoined.substring(0, allPrefsJoined.length - 2)

        val cv = ContentValues()
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, allQualsJoined)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, allPrefsJoined)

        var jobString = mCursor!!.getString(mCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS))
        val jobs = LinkedList(Arrays.asList(*jobString.split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        if (newJob) {

            jobs.add(tvJobName!!.text.toString())

            jobString = ""

            for (s in jobs) {

                jobString += "$s||"
            }

            jobString = jobString.substring(0, jobString.length - 2)

            cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString)

        } else {

            jobs[jobID] = tvJobName!!.text.toString()

            jobString = ""

            for (s in jobs) {

                jobString += "$s||"
            }

            jobString = jobString.substring(0, jobString.length - 2)

            cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString)
        }

        mEventDb!!.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry._ID + "=" + eventID, null
        )
    }

    fun deleteThenStoreJobData() {

        quals = mQualAdapter!!.quals
        prefs = mPrefAdapter!!.prefs

        var qualJoined = ""

        Log.d(TAG, "DEBUG JDA 311 $quals")

        for (s in quals) {

            qualJoined += "$s||"
        }

        if (quals.size > 0)
            qualJoined = qualJoined.substring(0, qualJoined.length - 2)

        Log.d(TAG, "DEBUG JDA 324 $qualJoined")

        allQuals[jobID] = qualJoined

        var prefJoined = ""

        for (s in prefs) {

            prefJoined += "$s||"
        }

        if (prefs.size > 0)
            prefJoined = prefJoined.substring(0, prefJoined.length - 2)

        allPrefs[jobID] = prefJoined

        var allQualsJoined = ""

        for (s in allQuals) {

            allQualsJoined += "$s//"
        }

        if (allQuals.size > 0)
            allQualsJoined = allQualsJoined.substring(0, allQualsJoined.length - 2)

        Log.d(TAG, "DEBUG JDA 374 $allQualsJoined")

        var allPrefsJoined = ""

        for (s in allPrefs) {

            allPrefsJoined += "$s//"
        }

        if (allPrefs.size > 0)
            allPrefsJoined.substring(0, allPrefsJoined.length - 2)

        Log.d(TAG, "DEBUG JDA 607 allqualsjoined $allQualsJoined allprefsjoined $allPrefsJoined")

        val jobs = LinkedList(Arrays.asList(*mCursor!!.getString(mCursor!!.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS)).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

        jobs.removeAt(jobID)

        var jobString = ""

        for (s in jobs) {

            jobString += "$s||"
        }

        if (jobs.size > 0)
            jobString.substring(0, jobString.length - 2)

        Log.d(TAG, "DEBUG JDA 584 $jobString")

        val cv = ContentValues()
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, allQualsJoined)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, allPrefsJoined)
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString)

        mEventDb!!.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry._ID + "=" + eventID, null
        )

        mCursor = getEventData(eventName)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
    }

    fun updateNullView(adapter: RecyclerView.Adapter<*>, textView: TextView?) {
        if (adapter.itemCount == 0) {

            textView!!.visibility = View.VISIBLE
        } else {

            textView!!.visibility = View.GONE
        }
    }

    fun nameOnClick() {

        famJDA!!.close(true)

        val jobNAD = Useful.NameAlertDialog("Job", "DONE", "CANCEL", tvJobName!!.text.toString(), EditText(this@JobDetailsActivity), this@JobDetailsActivity)
        jobNAD.create()
        jobNAD.ad.setOnShowListener {
            jobNAD.setBtnColor()
            jobNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                tvJobName!!.text = jobNAD.et.text.toString()
                jobNAD.ad.dismiss()
            }
        }

        jobNAD.ad.show()
    }
}
