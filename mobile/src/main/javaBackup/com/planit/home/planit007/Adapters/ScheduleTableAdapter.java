package planit007.planit.home.planit007.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evrencoskun.tableview.adapter.AbstractTableAdapter;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;
import com.planit.home.planit003.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 2017-12-17.
 */

public class ScheduleTableAdapter extends AbstractTableAdapter<String, String, String> {

    static Context c;

    String TAG = this.getClass().getSimpleName();

    //column by column, ie .get(2).get(1) would be third column second cell
    //holds holders, used to match width of cell views
    private List<AbstractViewHolder> holders = new ArrayList<AbstractViewHolder>();

    public ScheduleTableAdapter(Context context) {

        super(context);
        c = context;
    }

    /**
     * This is sample CellViewHolder class
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    class MyCellViewHolder extends AbstractViewHolder {

        public final TextView cell_textview;

        public MyCellViewHolder(View itemView) {
            super(itemView);
            cell_textview = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_schedule_member);
        }
    }


    /**
     * This is where you create your custom Cell ViewHolder. This method is called when Cell
     * RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given type to
     * represent an item.
     *
     * @param viewType : This value comes from #getCellItemViewType method to support different type
     *                 of viewHolder as a Cell item.
     *
     * @see #getCellItemViewType(int);
     */
    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        // Get cell xml layout
        View layout = LayoutInflater.from(m_jContext).inflate(planit.planit.home.planit003.R.layout.schedule_member_list_item,
                parent, false);
        // Create a Custom ViewHolder for a Cell item.
        return new MyCellViewHolder(layout);
    }

    /**
     * That is where you set Cell View Model data to your custom Cell ViewHolder. This method is
     * Called by Cell RecyclerView of the TableView to display the data at the specified position.
     * This method gives you everything you need about a cell item.
     *
     * @param holder       : This is one of your cell ViewHolders that was created on
     *                     ```onCreateCellViewHolder``` method. In this example we have created
     *                     "MyCellViewHolder" holder.
     * @param p_jValue     : This is the cell view model located on this X and Y position. In this
     *                     example, the model class is "Cell".
     * @param p_nXPosition : This is the X (Column) position of the cell item.
     * @param p_nYPosition : This is the Y (Row) position of the cell item.
     *
     * @see #onCreateCellViewHolder(ViewGroup, int);
     */
    @Override
    public void onBindCellViewHolder(AbstractViewHolder holder, Object p_jValue, int
            p_nXPosition, int p_nYPosition) {

        // Get the holder to update cell item text
        MyCellViewHolder viewHolder = (MyCellViewHolder) holder;
        viewHolder.cell_textview.setText((String) p_jValue);

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can ignore them.

        //holder.setBackgroundColor(c.getResources().getColor(R.color.white));

        for(AbstractViewHolder h : holders) {

            h.itemView.setBackgroundColor(c.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
        }

        Log.d(TAG, "DEBUG STA 109 " + p_jValue);
        viewHolder.itemView.setBackgroundColor(c.getResources().getColor(planit.planit.home.planit003.R.color.white));

        // It is necessary to remeasure itself.
        viewHolder.cell_textview.requestLayout();
    }


    /**
     * This is sample CellViewHolder class.
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    class MyColumnHeaderViewHolder extends AbstractViewHolder {

        public final TextView column_textview;

        public MyColumnHeaderViewHolder(View itemView) {
            super(itemView);
            column_textview = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_schedule_job);
        }

        public void setWidth(int w) {

            column_textview.getLayoutParams().width = w;
        }
    }

    /**
     * This is where you create your custom Column Header ViewHolder. This method is called when
     * Column Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getColumnHeaderItemViewType" method to support
     *                 different type of viewHolder as a Column Header item.
     *
     * @see #getColumnHeaderItemViewType(int);
     */
    @Override
    public RecyclerView.ViewHolder onCreateColumnHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Column Header xml Layout
        View layout = LayoutInflater.from(m_jContext).inflate(planit.planit.home.planit003.R.layout
                .schedule_job_list_item, parent, false);

        // Create a ColumnHeader ViewHolder
        return new MyColumnHeaderViewHolder(layout);
    }

    /**
     * That is where you set Column Header View Model data to your custom Column Header ViewHolder.
     * This method is Called by ColumnHeader RecyclerView of the TableView to display the data at
     * the specified position. This method gives you everything you need about a column header
     * item.
     *
     * @param holder   : This is one of your column header ViewHolders that was created on
     *                 ```onCreateColumnHeaderViewHolder``` method. In this example we have created
     *                 "MyColumnHeaderViewHolder" holder.
     * @param p_jValue : This is the column header view model located on this X position. In this
     *                 example, the model class is "ColumnHeader".
     * @param position : This is the X (Column) position of the column header item.
     *
     * @see #onCreateColumnHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindColumnHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int
            position) {

        // Get the holder to update cell item text
        MyColumnHeaderViewHolder columnHeaderViewHolder = (MyColumnHeaderViewHolder) holder;
        columnHeaderViewHolder.column_textview.setText((String) p_jValue);

        // If your TableView should have auto resize for cells & columns.
        // Then you should consider the below lines. Otherwise, you can ignore them.
        holders.add(holder);

        // It is necessary to remeasure itself.
        columnHeaderViewHolder.column_textview.setTextColor(c.getResources().getColor(planit.planit.home.planit003.R.color.white));
        columnHeaderViewHolder.itemView.setBackgroundColor(c.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
        columnHeaderViewHolder.itemView.getLayoutParams().width = LinearLayout
                .LayoutParams.WRAP_CONTENT;
        columnHeaderViewHolder.column_textview.requestLayout();
    }

    /**
     * This is sample CellViewHolder class.
     * This viewHolder must be extended from AbstractViewHolder class instead of RecyclerView.ViewHolder.
     */
    class MyRowHeaderViewHolder extends AbstractViewHolder {

        public final TextView row_textview;

        public MyRowHeaderViewHolder(View itemView) {
            super(itemView);
            row_textview = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_schedule_timeslot);
        }
    }


    /**
     * This is where you create your custom Row Header ViewHolder. This method is called when
     * Row Header RecyclerView of the TableView needs a new RecyclerView.ViewHolder of the given
     * type to represent an item.
     *
     * @param viewType : This value comes from "getRowHeaderItemViewType" method to support
     *                 different type of viewHolder as a row Header item.
     *
     * @see #getRowHeaderItemViewType(int);
     */
    @Override
    public RecyclerView.ViewHolder onCreateRowHeaderViewHolder(ViewGroup parent, int viewType) {

        // Get Row Header xml Layout
        View layout = LayoutInflater.from(m_jContext).inflate(planit.planit.home.planit003.R.layout
                .schedule_timeslot_list_item, parent, false);

        // Create a Row Header ViewHolder
        return new MyRowHeaderViewHolder(layout);
    }


    /**
     * That is where you set Row Header View Model data to your custom Row Header ViewHolder. This
     * method is Called by RowHeader RecyclerView of the TableView to display the data at the
     * specified position. This method gives you everything you need about a row header item.
     *
     * @param holder   : This is one of your row header ViewHolders that was created on
     *                 ```onCreateRowHeaderViewHolder``` method. In this example we have created
     *                 "MyRowHeaderViewHolder" holder.
     * @param p_jValue : This is the row header view model located on this Y position. In this
     *                 example, the model class is "RowHeader".
     * @param position : This is the Y (row) position of the row header item.
     *
     * @see #onCreateRowHeaderViewHolder(ViewGroup, int) ;
     */
    @Override
    public void onBindRowHeaderViewHolder(AbstractViewHolder holder, Object p_jValue, int
            position) {

        // Get the holder to update row header item text
        MyRowHeaderViewHolder rowHeaderViewHolder = (MyRowHeaderViewHolder) holder;
        rowHeaderViewHolder.row_textview.setText((String) p_jValue);

        Log.d(TAG, "DEBUG STA 258 " + p_jValue);

        holders.add(holder);

        rowHeaderViewHolder.row_textview.setTextColor(c.getResources().getColor(planit.planit.home.planit003.R.color.white));
        rowHeaderViewHolder.row_textview.setBackgroundColor(c.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
        rowHeaderViewHolder.itemView.getLayoutParams().height = LinearLayout
                .LayoutParams.WRAP_CONTENT;
        rowHeaderViewHolder.row_textview.requestLayout();
    }

    @Override
    public View onCreateCornerView() {
        // Get Corner xml layout
        return LayoutInflater.from(m_jContext).inflate(planit.planit.home.planit003.R.layout.schedule_corner, null);
    }

    @Override
    public int getColumnHeaderItemViewType(int position) {
        // The unique ID for this type of column header item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0;
    }

    @Override
    public int getRowHeaderItemViewType(int pYPosition) {
        // The unique ID for this type of row header item
        // If you have different items for Row Header View by Y (Row) position,
        // then you should fill this method to be able create different
        // type of RowHeaderViewHolder on "onCreateRowHeaderViewHolder"
        return 0;
    }

    @Override
    public int getCellItemViewType(int pXPosition) {
        // The unique ID for this type of cell item
        // If you have different items for Cell View by X (Column) position,
        // then you should fill this method to be able create different
        // type of CellViewHolder on "onCreateCellViewHolder"
        return 0;
    }

    @Override
    public void setAllItems(List<String> p_jColumnHeaderItems, List<String> p_jRowHeaderItems, List<List<String>> p_jCellItems) {

        super.setAllItems(p_jColumnHeaderItems, p_jRowHeaderItems, p_jCellItems);
    }
}