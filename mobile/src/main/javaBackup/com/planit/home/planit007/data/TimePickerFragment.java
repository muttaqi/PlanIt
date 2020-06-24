package planit007.planit.home.planit007.data;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import com.planit.home.planit003.R;

/**
 * Created by Home on 2018-07-30.
 */

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
    int minute = 0;
    int hour = 0;

    TimePickerDialog tpd;

    public void setTimes(int hr, int min) {

        this.hour = hr;
        this.minute = min;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){


        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

        // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
        tpd = new TimePickerDialog(getActivity(),
                planit.planit.home.planit003.R.style.Theme_Dialog,this,hour,minute,false);

        // Return the TimePickerDialog
        return tpd;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute){

        tpd.onTimeChanged(view, hourOfDay, minute);
    }

    public TimePickerDialog getTpd() {
        return tpd;
    }

    public void setTpd(TimePickerDialog tp) {
        tpd = tp;
    }

    public int getHour() { return hour; }

    public int getMinute() { return minute; }
}
