package planit007.planit.home.planit007.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.home.planit003.R;

import java.util.List;

/**
 * Created by Home on 2017-08-23.
 */

public class PreferenceAdapter extends RecyclerView.Adapter<PreferenceAdapter.PreferenceViewHolder>{

    private final String TAG = PreferenceAdapter.class.getSimpleName();

    private Context mContext;

    private Cursor mCursor;

    TextView prefNullView;

    List<String> prefs;

    public PreferenceAdapter(Context context, List<String> inPrefs, TextView nullView) {

        mContext = context;

        prefNullView = nullView;

        for (String p : inPrefs) {

            if (p.equals("")) {

                inPrefs.remove(p);
            }
        }

        prefs = inPrefs;
    }

    class PreferenceViewHolder extends RecyclerView.ViewHolder {

        TextView prefView;
        ImageButton deleteView;

        public PreferenceViewHolder(View itemView) {

            super(itemView);

            prefView = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_pref);

            deleteView = (ImageButton) itemView.findViewById(planit.planit.home.planit003.R.id.ib_item_pref);
        }
    }

    @Override
    public PreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        boolean shouldAttachImmediatelyToParent = false;

        View view = inflater.inflate(planit.planit.home.planit003.R.layout.pref_list_item, parent, shouldAttachImmediatelyToParent);
        PreferenceViewHolder viewHolder = new PreferenceViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder, final int position) {

        String preference = prefs.get(position);

        Log.d(TAG, "DEBUG PAD 76|" + preference + "|");

            holder.prefView.setText(preference);

            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d(TAG, "DEBUG ONCLICK PAD " + position);

                    Toast.makeText(mContext, "Removed " + prefs.get((int) (position)), Toast.LENGTH_SHORT).show();

                    prefs.remove(position);

                    notifyDataSetChanged();
                    notifyItemRangeChanged(position, prefs.size());

                    updateNullView(prefNullView);
                }
            });
    }

    public List<String> getPrefs() {

        return prefs;
    }

    @Override
    public int getItemCount() {
        return prefs.size();
    }

    public void updateNullView(TextView textView) {
        if (prefs.size() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }
}