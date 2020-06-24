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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.planit.mobile.Adapters.EventAdapter
import com.planit.mobile.Adapters.MemberPreferenceAdapter
import com.planit.mobile.Adapters.MemberQualificationAdapter
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.DbHelpers.MemberDbHelper
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.Useful

import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedList

class MemberDetailsActivity : AppCompatActivity(), MemberPreferenceAdapter.MemberPreferenceAdapterOnClickHandler, MemberQualificationAdapter.MemberQualificationAdapterOnClickHandler, EventAdapter.EventAdapterOnClickHandler {
    internal var memberExists = false

    internal lateinit var defaultName: String
    internal var memberID: Int = 0
    internal var groupID: Int = 0

    internal var header: TextView? = null

    internal lateinit var tvName: TextView
    //Eventually will be extra from group details activity
    internal var members = ""

    private var mMemberDb: SQLiteDatabase? = null
    private var mEventsDb: SQLiteDatabase? = null

    private var memberQualificationAdapter: MemberQualificationAdapter? = null
    private var memberPreferenceAdapter: MemberPreferenceAdapter? = null
    private var eventAdapter: EventAdapter? = null

    private var memQualList: RecyclerView? = null
    private var memPrefList: RecyclerView? = null
    private var eventList: RecyclerView? = null

    private var txtNullQual: TextView? = null
    private var txtNullPref: TextView? = null
    private var txtNullEvent: TextView? = null

    private var mCursor: Cursor? = null
    private var eventCursor: Cursor? = null

    private var qualCursor: Cursor? = null
    private var prefCursor: Cursor? = null
    private val globalQuals = ArrayList<String>()
    private val globalPrefs = ArrayList<String>()

    private var quals: MutableList<String> = ArrayList()
    private var prefs: MutableList<String> = ArrayList()

    private var famMDA: FloatingActionMenu? = null

    private var created: Boolean = false

    var mContext: Context = this

    private var mAdView: AdView? = null

    val member: Cursor
        get() = mMemberDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null,
                MemberContract.MemberEntry._ID + " = " + memberID, null, null, null, null
        )

    val allEvents: Cursor
        get() = mEventsDb!!.query(
                EventContract.EventEntry.TABLE_NAME, null, null, null, null, null, null
        )

    val allMembers: Cursor
        get() = mMemberDb!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null, null, null, null, null, null
        )

    private val groupEvents: Cursor
        get() = mEventsDb!!.query(

                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry.COLUMN_GROUP_ID + "=" + groupID, null, null, null,
                EventContract.EventEntry.COLUMN_EVENT_NAME
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_member_details)

        mAdView = findViewById(R.id.md_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        val intent = intent

        defaultName = intent.getStringExtra("name")
        memberID = intent.getIntExtra("memberID", -1)
        groupID = intent.getIntExtra("groupID", 0)

        Log.d(TAG, "DEBUG MDA 111 $memberID")

        created = intent.getBooleanExtra("created", false)

        if (memberID != -1) {

            memberExists = true
        }

        tvName = findViewById<View>(R.id.md_tv_name) as TextView

        tvName.text = defaultName

        memQualList = findViewById<View>(R.id.md_rv_quals) as RecyclerView
        memPrefList = findViewById<View>(R.id.md_rv_prefs) as RecyclerView
        eventList = findViewById<View>(R.id.md_rv_events) as RecyclerView

        val qualLM = LinearLayoutManager(this)
        val prefLM = LinearLayoutManager(this)
        val eventLM = LinearLayoutManager(this)

        memQualList!!.layoutManager = qualLM
        memPrefList!!.layoutManager = prefLM
        eventList!!.layoutManager = eventLM

        val memberDbHelper = MemberDbHelper(this)
        val eventDbHelper = EventDbHelper(this)

        mMemberDb = memberDbHelper.writableDatabase
        mEventsDb = eventDbHelper.writableDatabase

        mCursor = member
        eventCursor = groupEvents

        eventCursor!!.moveToFirst()

        if (mCursor!!.moveToFirst()) {

            quals = LinkedList(Arrays.asList(*mCursor!!.getString(mCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS)).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
            prefs = LinkedList(Arrays.asList(*mCursor!!.getString(mCursor!!.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES)).split("\\|\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))

            quals.remove("")
            prefs.remove("")
        }

        Useful.removeUnderscores(quals)
        Useful.removeUnderscores(prefs)

        memberQualificationAdapter = MemberQualificationAdapter(this, quals, this)
        memberPreferenceAdapter = MemberPreferenceAdapter(this, prefs, this)
        eventAdapter = EventAdapter(this, eventCursor, this)

        txtNullQual = findViewById<View>(R.id.md_tv_qual_null) as TextView
        txtNullPref = findViewById<View>(R.id.md_tv_pref_null) as TextView
        txtNullEvent = findViewById<View>(R.id.md_tv_event_null) as TextView

        updateNullView(memberQualificationAdapter!!, txtNullQual)
        updateNullView(memberPreferenceAdapter!!, txtNullPref)
        updateNullView(eventAdapter!!, txtNullEvent)

        memQualList!!.adapter = memberQualificationAdapter
        memPrefList!!.adapter = memberPreferenceAdapter
        eventList!!.adapter = eventAdapter

        qualCursor = allEvents

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

        famMDA = findViewById<View>(R.id.md_fam) as FloatingActionMenu

        findViewById<View>(R.id.md_fab_add_qual).setOnClickListener {
            var qualsArr = arrayOfNulls<String>(globalQuals.size)
            qualsArr = globalQuals.toTypedArray<String?>()
            val actvAdapter = ArrayAdapter(mContext,
                    R.layout.custom_expandable_list_item, qualsArr)

            val actv = AutoCompleteTextView(this@MemberDetailsActivity)
            actv.setAdapter(actvAdapter)

            val qualNAD = Useful.NameAlertDialog("Qualification", "ADD", "CANCEL", "", actv, this@MemberDetailsActivity)
            qualNAD.create()
            qualNAD.ad.setOnShowListener {
                qualNAD.setBtnColor()

                val btnPos = qualNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE)
                btnPos.setOnClickListener {
                    val name = tvName.text.toString()

                    if (qualNAD.actv.text.toString() == "" || qualNAD.actv.text.toString() == null) {

                        val t = Toast.makeText(this@MemberDetailsActivity, "Please enter a qualification name", Toast.LENGTH_LONG)

                        t.show()
                    } else if (quals.size >= 1 && quals[quals.size - 1] == "") {

                        quals[quals.size - 1] = qualNAD.actv.text.toString()
                    } else {

                        quals.add(qualNAD.actv.text.toString())
                    }

                    if (!memberExists) {

                        addNewMember(name)

                        memberExists = true
                    } else {

                        updateMember(name)
                    }

                    memberQualificationAdapter!!.notifyDataSetChanged()

                    updateNullView(memberQualificationAdapter!!, txtNullQual)

                    qualNAD.ad.dismiss()

                    famMDA!!.close(true)
                }
            }

            qualNAD.ad.show()
        }

        findViewById<View>(R.id.md_fab_add_pref).setOnClickListener {
            var prefsArr = arrayOfNulls<String>(globalPrefs.size)
            prefsArr = globalPrefs.toTypedArray<String?>()
            val actvAdapter = ArrayAdapter(mContext,
                    R.layout.custom_expandable_list_item, prefsArr)
            val actv = AutoCompleteTextView(this@MemberDetailsActivity)
            actv.setAdapter(actvAdapter)

            val prefNAD = Useful.NameAlertDialog("Preference", "ADD", "CANCEL", "", actv, this@MemberDetailsActivity)
            prefNAD.create()
            prefNAD.ad.setOnShowListener {
                prefNAD.setBtnColor()

                val btnPos = prefNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE)
                btnPos.setOnClickListener {
                    val name = tvName.text.toString()

                    if (prefNAD.actv.text.toString() == "" || prefNAD.actv.text.toString() == null) {

                        val t = Toast.makeText(this@MemberDetailsActivity, "Please enter a qualification name", Toast.LENGTH_LONG)

                        t.show()
                    } else if (prefs.size >= 1 && prefs[prefs.size - 1] == "") {

                        prefs[prefs.size - 1] = prefNAD.actv.text.toString()
                    } else {

                        prefs.add(prefNAD.actv.text.toString())
                    }

                    if (!memberExists) {

                        addNewMember(name)

                        memberExists = true
                    } else {

                        updateMember(name)
                    }

                    memberPreferenceAdapter!!.notifyDataSetChanged()

                    updateNullView(memberPreferenceAdapter!!, txtNullPref)

                    prefNAD.ad.dismiss()

                    famMDA!!.close(true)
                }
            }

            prefNAD.ad.show()
        }

        tvName.setOnClickListener { nameOnClick() }

        findViewById<View>(R.id.md_iv_name).setOnClickListener { nameOnClick() }

        findViewById<View>(R.id.md_iv_qual).setOnClickListener {
            val adb = AlertDialog.Builder(mContext)

            val tv = TextView(adb.context)
            tv.text = "Qualifications allow you to control which members are scheduled for certain jobs. " +
                    "Just add a qualification to a job, and add a qualification of the same name to one or more members, " +
                    "and only those members will be scheduled for the job. Be careful though, if no member with the " +
                    "qualification is available, you won't be able to create a schedule!"
            tv.setPadding(24, 0, 0, 0)

            adb.setPositiveButton("CLOSE") { dialogInterface, i -> }.setTitle("Qualifications").setView(tv)

            val ad = adb.create()

            ad.setOnShowListener { ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary)) }
            ad.show()
        }

        findViewById<View>(R.id.md_iv_pref).setOnClickListener {
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

        val inflater = menuInflater

        inflater.inflate(R.menu.save_menu, menu)
        inflater.inflate(R.menu.delete_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.save_button -> {

                //returns whether valid  first & last name
                if (saveMember()) {

                    val intent = Intent(this@MemberDetailsActivity, MainActivity::class.java)

                    startActivity(intent)
                }

                return true
            }

            R.id.delete_button -> {

                val name = tvName.text.toString()

                memberDeletePrompt(name).show()

                return true
            }

            else ->

                return super.onOptionsItemSelected(item)
        }

    }

    fun addNewMember(name: String): Long {

        val cv = ContentValues()

        cv.put(MemberContract.MemberEntry.COLUMN_NAME, name)
        cv.put(MemberContract.MemberEntry.COLUMN_GROUP_ID, groupID)
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, "")

        var qualString = ""
        var prefString = ""

        for (s in quals) {

            qualString += "$s||"
        }

        if (quals.size > 0)
            qualString = qualString.substring(0, qualString.length - 2)

        for (s in prefs) {

            prefString += "$s||"
        }

        if (prefs.size > 0)
            prefString = prefString.substring(0, prefString.length - 2)

        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS, qualString)
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES, prefString)

        return mMemberDb!!.insertOrThrow(MemberContract.MemberEntry.TABLE_NAME, null, cv)
    }

    fun updateMember(name: String): Long {

        Log.d(TAG, "DEBUG 561 $name")

        val cv = ContentValues()

        cv.put(MemberContract.MemberEntry.COLUMN_NAME, name)

        var qualString = ""
        var prefString = ""

        for (s in quals) {

            qualString += "$s||"
        }

        if (quals.size > 0)
            qualString = qualString.substring(0, qualString.length - 2)

        for (s in prefs) {

            prefString += "$s||"
        }

        if (prefs.size > 0)
            prefString = prefString.substring(0, prefString.length - 2)

        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS, qualString)
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES, prefString)

        return mMemberDb!!.update(
                MemberContract.MemberEntry.TABLE_NAME,
                cv,
                MemberContract.MemberEntry._ID + " = " + memberID, null
        ).toLong()
    }

    fun memberDeletePrompt(name: String): AlertDialog {

        val adb = AlertDialog.Builder(this)

        adb
                .setNegativeButton("CANCEL") { dialogInterface, i -> }
                .setPositiveButton("CONTINUE") { dialogInterface, i ->
                    removeMember(memberID)

                    val intent = Intent(this@MemberDetailsActivity, MainActivity::class.java)

                    startActivity(intent)
                }
                .setTitle("Are you sure you would like to delete this member?")

        val retPrompt = adb.create()

        retPrompt.setOnShowListener {
            retPrompt.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorPrimary))
            retPrompt.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.colorPrimary))
        }

        return retPrompt
    }

    fun removeMember(mID: Int): Boolean {

        return mMemberDb!!.delete(MemberContract.MemberEntry.TABLE_NAME, MemberContract.MemberEntry._ID + "=" + mID, null) > 0
    }

    fun saveMember(): Boolean {

        if (tvName.text.toString() == "" || tvName.text.toString() == null) {

            val t = Toast.makeText(this@MemberDetailsActivity, "Please enter a name", Toast.LENGTH_LONG)

            t.show()

            return false
        } else if (tvName.text.toString().contains("|")) {

            val t = Toast.makeText(this@MemberDetailsActivity, "Special characters such as '|' are not allowed", Toast.LENGTH_LONG)

            t.show()

            return false
        } else {

            val name = tvName.text.toString()

            if (!memberExists) {

                addNewMember(name)

                memberExists = true
            } else {

                updateMember(name)
            }

            return true
        }
    }

    override fun onClickEvent(event_id: Int, group_id: Int) {

        Log.d(TAG, "DEBUG MDA 754")
        saveMember()

        //whether or not created UPON ENTERING ACTIVITY (would have been created above)
        if (!created) {

            /*
            mCursor = getAllMembers();
            mCursor.moveToLast();

            memberID = mCursor.getInt(mCursor.getColumnIndex(MemberContract.MemberEntry._ID));*/
        }

        val i = Intent(this@MemberDetailsActivity, MemberAvailabilityActivity::class.java)
        i.putExtra("eventID", event_id)
        i.putExtra("groupID", groupID)
        i.putExtra("name", tvName.text.toString())
        i.putExtra("memberID", memberID)

        startActivity(i)
    }

    override fun onClick(preference_id: Long, isDelete: String) {

        if (isDelete == "true") {

            updateNullView(memberPreferenceAdapter!!, txtNullPref)
        } else {

            //if change name functionality is desired
        }
    }

    override fun onClick(qualification_id: Long, isDelete: Boolean) {

        if (isDelete) {

            updateNullView(memberQualificationAdapter!!, txtNullQual)
        } else {

            //if change name functionality is desired
        }
    }

    fun updateNullView(adapter: RecyclerView.Adapter<*>, textView: TextView?) {

        if (adapter.itemCount == 0) {

            textView!!.visibility = View.VISIBLE
        } else {

            textView!!.visibility = View.GONE
        }
    }

    fun nameOnClick() {

        famMDA!!.close(true)

        tvName.paintFlags = tvName.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()

        val memberNAD = Useful.NameAlertDialog("Member", "DONE", "CANCEL", tvName.text.toString(), EditText(this@MemberDetailsActivity), this@MemberDetailsActivity)
        memberNAD.create()
        memberNAD.ad.setOnShowListener {
            memberNAD.setBtnColor()

            memberNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                defaultName = memberNAD.et.text.toString()
                tvName.text = defaultName

                memberNAD.ad.dismiss()
            }
        }

        memberNAD.ad.show()
    }

    companion object {
        private val TAG = "MDA"
    }
}
