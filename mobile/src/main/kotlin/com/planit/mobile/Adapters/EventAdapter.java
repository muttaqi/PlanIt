package com.planit.mobile.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.data.DbHelpers.GroupDbHelper;
import com.planit.mobile.R;

/**
 * Created by Home on 2017-08-01.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder>{

    //Used for logs
    private static final String TAG = EventAdapter.class.getSimpleName();

    private Context mContext;

    private GroupDbHelper groupDbHelper;
    private SQLiteDatabase groupDB;

    //Holds number of items
    private Cursor mCursor;

    private final EventAdapterOnClickHandler mClickHandler;

    public interface EventAdapterOnClickHandler {

        void onClickEvent(int event_id, int group_id);
    }

    //Constructor
    public EventAdapter(Context context, Cursor cursor, EventAdapterOnClickHandler listener) {

        groupDbHelper = new GroupDbHelper(context);
        groupDB = groupDbHelper.getReadableDatabase();

        this.mContext = context;
        mCursor = cursor;

        mClickHandler = listener;
    }


    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listItemEventNameView;
        TextView listItemEventDateView;

        public EventViewHolder(View itemView) {

            super(itemView);

            listItemEventNameView = (TextView) itemView.findViewById(R.id.el_tv_event_name);
            listItemEventDateView = (TextView) itemView.findViewById(R.id.el_tv_event_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int eventID = Integer.valueOf(String.valueOf(view.getTag(R.id.eventid)));
            int groupID = Integer.valueOf(String.valueOf(view.getTag(R.id.groupid)));

            mClickHandler.onClickEvent(eventID, groupID);
        }
    }


    //Inflates text view
    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.event_list_item, viewGroup, shouldAttachToParentImmediately);
        EventViewHolder viewHolder = new EventViewHolder(view);

        return viewHolder;
    }


    public void onBindViewHolder(EventViewHolder holder, int position) {

        if(!mCursor.moveToPosition(position)) {

            return;
        }

        //display code
        String eventName = mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.Companion.getCOLUMN_EVENT_NAME()));
        String eventDate = mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.Companion.getCOLUMN_EVENT_DATE()));

        int groupID = mCursor.getInt(mCursor.getColumnIndex(EventContract.EventEntry.Companion.getCOLUMN_GROUP_ID()));

        Cursor groupCursor = groupDB.query(
                GroupContract.GroupEntry.Companion.getTABLE_NAME(),
                null,
                GroupContract.GroupEntry._ID + "=" + groupID,
                null,
                null,
                null,
                GroupContract.GroupEntry._ID,
                null
        );

        holder.listItemEventNameView.setText(eventName);
        holder.listItemEventDateView.setText(eventDate);

        holder.itemView.setTag(R.id.eventid, mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry._ID)));
        holder.itemView.setTag(R.id.groupid, groupID);

        mCursor.moveToNext();
    }

    //returns number of items
    public int getItemCount() {return mCursor.getCount();}
}
