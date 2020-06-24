package com.planit.mobile.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Home on 2017-08-17.
 */

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private static final String TAG = JobAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;

    private String jobString = "";
    private List<String> jobs = new ArrayList<String>();

    private final JobAdapterOnClickHandler mClickHandler;

    public interface JobAdapterOnClickHandler {

        void onClick(String job, int jobID);
    }

    public JobAdapter(Context context, Cursor cursor,JobAdapterOnClickHandler listener) {

        this.mCursor = cursor;
        this.mContext = context;

        if(mCursor.moveToFirst() && mCursor != null) {
            jobString = mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.Companion.getCOLUMN_TABLE_JOBS()));
            jobs = new LinkedList<String>(Arrays.asList(jobString.split("\\|\\|")));

            for (int i = 0; i < jobs.size(); i ++) {

                if (jobs.get(i).equals("")) {

                    jobs.remove(i);
                }
            }
        }

        mClickHandler = listener;
    }

    class JobViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvJobItem;

        public JobViewHolder(View itemView) {

            super(itemView);

            tvJobItem = (TextView) itemView.findViewById(R.id.tv_item_job);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int jobID = (int) (view.getTag(R.id.jobID));
            String job = String.valueOf(view.getTag(R.id.job));

            mClickHandler.onClick(job, jobID);
        }
    }

    public JobViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.job_list_item, viewGroup, shouldAttachToParentImmediately);
        JobAdapter.JobViewHolder viewHolder = new JobAdapter.JobViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, final int position) {

        String job = jobs.get(position);

        holder.tvJobItem.setText(job);

        holder.itemView.setTag(R.id.job, job);
        holder.itemView.setTag(R.id.jobID, position);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }
}
