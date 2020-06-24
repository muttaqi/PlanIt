package planit007.planit.home.planit007.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.EventContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Home on 2017-08-17.
 */

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private static final String TAG = TimeSlotAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor eventCursor;
    private Cursor mMemberCursor;

    private int eventID;

    private List<String> timeSlotStarts = new ArrayList<String>();
    private String timeSlotStartString;

    private List<String> timeSlotEnds = new ArrayList<String>();
    private String timeSlotEndString;

    private List<String[]> memberInavailability = new ArrayList<String[]>();

    private final TimeSlotAdapterOnClickHandler mClickHandler;

    boolean hasCheckBoxMAA;

    public interface TimeSlotAdapterOnClickHandler {

        void onClick(String timeSlotStart, String timeSlotEnd, int timeSlotID, View view, CheckBox checkBox);
    }

    public TimeSlotAdapter(Context context, Cursor eventCursor, String availFromCursor, TimeSlotAdapterOnClickHandler listener, boolean doesHaveCheckBoxMAA) {

        hasCheckBoxMAA = doesHaveCheckBoxMAA;

        Log.d(TAG, "DEBUG TSAD 55 " + hasCheckBoxMAA);

        this.eventCursor = eventCursor;
        this.mContext = context;
        this.eventID = eventCursor.getInt(eventCursor.getColumnIndex(EventContract.EventEntry._ID));

        Log.d(TAG, "TSAD 63 " + eventID);

        mClickHandler = listener;

        if(this.eventCursor != null && this.eventCursor.moveToFirst()) {

            timeSlotStartString = this.eventCursor.getString(this.eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES));
            timeSlotEndString = this.eventCursor.getString(this.eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES));

            timeSlotStarts = new LinkedList<String>(Arrays.asList(timeSlotStartString.split("\\|\\|")));
            timeSlotEnds = new LinkedList<String>(Arrays.asList(timeSlotEndString.split("\\|\\|")));

            Log.d(TAG, "DEBUG TSAD 77 " + timeSlotStartString);
            Log.d(TAG, "DEBUG TSAD 77 " + timeSlotEndString);

            boolean tryAgain = false;

            do {

                tryAgain = false;

                for (int i = 0; i < timeSlotStarts.size() - 1; i ++) {

                    Log.d(TAG, "DEBUG TSAD 66 " + timeSlotStarts.size());

                    if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2))) {

                        Log.d(TAG, "DEBUG TSAD 70 " + i);

                        String a = timeSlotStarts.get(i);
                        String b = timeSlotStarts.get(i + 1);

                        String c = timeSlotEnds.get(i);
                        String d = timeSlotEnds.get(i + 1);

                        timeSlotStarts.set(i, b);
                        timeSlotStarts.set(i + 1, a);

                        timeSlotEnds.set(i, d);
                        timeSlotEnds.set(i + 1, c);

                        tryAgain = true;
                    }

                    else if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) == Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2)) && Integer.valueOf(timeSlotStarts.get(i).substring(3, 5)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(3, 5))) {

                        String a = timeSlotStarts.get(i);
                        String b = timeSlotStarts.get(i + 1);

                        String c = timeSlotEnds.get(i);
                        String d = timeSlotEnds.get(i + 1);

                        timeSlotStarts.set(i, b);
                        timeSlotStarts.set(i + 1, a);

                        timeSlotEnds.set(i, d);
                        timeSlotEnds.set(i + 1, c);

                        tryAgain = true;
                    }
                }
            } while (tryAgain);

            if ((timeSlotStarts.size() == 1 && timeSlotStarts.get(0).equals("")) || (timeSlotEnds.size() == 1 && timeSlotEnds.get(0).equals(""))) {

                timeSlotStarts.remove(0);
                timeSlotEnds.remove(0);
            }
        }

        if (doesHaveCheckBoxMAA) {

            String avail = availFromCursor;

            if (!(avail.equals(null) || avail.equals(""))) {

                Log.d(TAG, "DEBUG TSAD 124 " + avail);

                List<String> tempList = new LinkedList<String>(Arrays.asList(avail.split("//")));
                for (String s : tempList) {

                    String[] sAvail = s.split("\\s+");

                    Log.d(TAG, "DEBUG TSAD 138 " + sAvail[0] + " " + eventID);

                    if (sAvail.length > 1) {

                        if (sAvail[0].equals(String.valueOf(eventID))) {

                            Log.d(TAG, "DEBUG TSAD 144");

                            memberInavailability.add(sAvail);
                        }
                    }
                }
            }
        }
    }

    class TimeSlotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimeSlotItem;
        ImageButton deleteView;
        CheckBox checkBox;

        public TimeSlotViewHolder(View itemView) {

            super(itemView);

            tvTimeSlotItem = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_timeslot);
            deleteView = (ImageButton) itemView.findViewById(planit.planit.home.planit003.R.id.ib_item_timeslot);
            checkBox = (CheckBox) itemView.findViewById(planit.planit.home.planit003.R.id.cb_item_timeslot);

            itemView.setOnClickListener(this);

            if (hasCheckBoxMAA) {

                deleteView.setVisibility(View.INVISIBLE);

                checkBox.setOnClickListener(this);
            }

            else {

                checkBox.setVisibility(View.INVISIBLE);

                deleteView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {

            Log.d(TAG, "DEBUG TSAD 87 " + view.getTag(planit.planit.home.planit003.R.id.timeslotstart));

            String timeSlotStart = String.valueOf(view.getTag(planit.planit.home.planit003.R.id.timeslotstart));
            String timeSlotEnd = String.valueOf(view.getTag(planit.planit.home.planit003.R.id.timeslotend));

            int timeSlotID = (int) view.getTag(planit.planit.home.planit003.R.id.timeslotid);

            mClickHandler.onClick(timeSlotStart, timeSlotEnd, timeSlotID, view, checkBox);
        }
    }

    public TimeSlotViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(planit.planit.home.planit003.R.layout.timeslot_list_item, viewGroup, shouldAttachToParentImmediately);
        TimeSlotAdapter.TimeSlotViewHolder viewHolder = new TimeSlotAdapter.TimeSlotViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TimeSlotViewHolder holder, final int position) {

        Log.d(TAG, "DEBUG TSAD 178 position " + position + " " + holder.itemView.getId());

        String timeSlotStart = timeSlotStarts.get(position);
        final String timeSlotEnd = timeSlotEnds.get(position);

        holder.tvTimeSlotItem.setText(timeSlotStart + " - " + timeSlotEnd);

        if (hasCheckBoxMAA) {

            holder.checkBox.toggle();

            Log.d(TAG, "DEBUG TSAD 224");

            for(int i = 0; i < memberInavailability.size(); i ++) {

                Log.d(TAG, "DEBUG TSAD 228 " + memberInavailability.get(i).length + " " + memberInavailability.get(i)[1]);

                if (memberInavailability.get(i).length > 1 && memberInavailability.get(i)[1].equals(String.valueOf(position))) {

                    Log.d(TAG, "DEBUG TSAD 195 " + memberInavailability.get(i)[1]);

                    holder.checkBox.toggle();
                    Log.d(TAG, "DEBUG TSAD 198 " + holder.checkBox.isChecked());
                }
            }
        }

        holder.itemView.setTag(planit.planit.home.planit003.R.id.timeslotstart, timeSlotStart);
        holder.itemView.setTag(planit.planit.home.planit003.R.id.timeslotend, timeSlotEnd);
        holder.checkBox.setTag(planit.planit.home.planit003.R.id.timeslotstart, timeSlotStart);
        holder.checkBox.setTag(planit.planit.home.planit003.R.id.timeslotend, timeSlotEnd);
        holder.deleteView.setTag(planit.planit.home.planit003.R.id.timeslotstart, timeSlotStart);
        holder.deleteView.setTag(planit.planit.home.planit003.R.id.timeslotend, timeSlotEnd);

        holder.itemView.setTag(planit.planit.home.planit003.R.id.timeslotid, position);
        holder.checkBox.setTag(planit.planit.home.planit003.R.id.timeslotid, position);
        holder.deleteView.setTag(planit.planit.home.planit003.R.id.timeslotid, position);
    }

    @Override
    public int getItemCount() {
        return timeSlotStarts.size();
    }
}