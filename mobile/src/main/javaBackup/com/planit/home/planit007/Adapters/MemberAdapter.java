package planit007.planit.home.planit007.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.MemberContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 2017-08-01.
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder>{

    //Used for logs
    private static final String TAG = MemberAdapter.class.getSimpleName();

    private Context mContext;

    //Holds number of items
    private Cursor mCursor;

    private final MemberAdapterOnClickHandler mClickHandler;

    List<String> names = new ArrayList<String>();
    List<Integer> ids = new ArrayList<>();

    public interface MemberAdapterOnClickHandler {

        void onClick(String name, int memberID);
    }

    //Constructor
    public  MemberAdapter(Context context, Cursor cursor, MemberAdapterOnClickHandler listener) {

        this.mContext = context;
        this.mCursor = cursor;

        if(mCursor.equals(null)) {

            Log.d(TAG, "DEBUG MA 50 MEMBER CURSOR IS BROKEN");
        }

        if(mCursor != null && mCursor.moveToFirst()) {
            do {

                names.add(mCursor.getString(mCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_NAME)));
            }

            while (mCursor.moveToNext());
        }

        Log.d(TAG, "DEBUG MAD 62 " + names);

        mCursor.moveToFirst();

        if(mCursor != null && mCursor.moveToFirst()) {

            do {

                ids.add(mCursor.getInt(mCursor.getColumnIndex(MemberContract.MemberEntry._ID)));
            }

            while (mCursor.moveToNext());
        }

        mClickHandler = listener;
    }


    class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView memberNameView;

        public MemberViewHolder(View itemView) {

            super(itemView);

            memberNameView = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_member);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            String name = String.valueOf(view.getTag(planit.planit.home.planit003.R.id.name));
            int id = (int) (view.getTag(planit.planit.home.planit003.R.id.memberid));

            mClickHandler.onClick(name, id);
        }
    }


    //Inflates text view
    public MemberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(planit.planit.home.planit003.R.layout.member_list_item, viewGroup, shouldAttachToParentImmediately);
        MemberViewHolder viewHolder = new MemberViewHolder(view);

        return viewHolder;
    }


    public void onBindViewHolder(MemberViewHolder holder, int position) {

        String name = names.get(position);
        int id = ids.get(position);

        holder.memberNameView.setText(name);

        holder.itemView.setTag(planit.planit.home.planit003.R.id.name, name);
        holder.itemView.setTag(planit.planit.home.planit003.R.id.memberid, id);
    }


    //returns number of items
    public int getItemCount() {return names.size();}
}
