package planit007.planit.home.planit007.data;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.planit.home.planit003.R;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 2017-08-02.
 */

public class Useful {

    static String TAG = Useful.class.getSimpleName();

    // item1 subitem1 || item1 subitem2 // item2 subitem1 || item2 subitem2 // item3 etc


    public static String concatLine(List<String> list) {

        String string = "";

        for (String s : list) {

            if(s.equals(list.get(list.size()))) {

                string += s;
            }

            else {

                string += s + "||";
            }
        }

        return string;
    }

    public static String concatLineAndSlash(List<List<String>> list) {

        String string = "";

        for (List<String> inList : list) {

            String inString = "";

            for (String s: inList) {

                if(s.equals(inList.get(inList.size()))) {

                    inString += s;
                }

                else {

                    inString += s + "||";
                }
            }

            if (inList.equals(list.get(list.size()))) {

                string += inString;
            }

            else {

                string += inString + "//";
            }
        }

        return string;
    }

    DateFormat formatter = new SimpleDateFormat("hh:mm a");

    public static void removeUnderscores(List<String> list) {

        for(String s : list) {

            if (s.equals("_")) {list.remove(s);}
        }

    }

    public static List<String> convertStringArrayToList(String[] sArray) {

        List<String> retList = new ArrayList<>();

        for (String s : sArray) {

            retList.add(s);
        }

        return retList;
    }

    public static String upperCaseAbbrev(String s) {

        String retS = "";

        for(int i = 0; i < s.length(); i ++) {

            if(Character.isUpperCase(s.charAt(i))) {

                retS += s.substring(i, i + 1);
            }
        }

        return retS;
    }

    public static String debugID(String TAG) {
        return "DEBUG " + upperCaseAbbrev(TAG) + " " + Thread.currentThread().getStackTrace()[3].getLineNumber() + " ";
    }

    public static String doubleSingleQuotations(String s) {

        for (int i = 0; i < s.length(); i ++) {

            if (s.substring(i, i + 1).equals("'")) {

                s = s.substring(0, i) + "'" + s.substring(i, s.length());
                i ++;
            }
        }

        return s;
    }

    public static class ThemedAlertDialog {

        public AlertDialog.Builder adb;
        public AlertDialog ad;

        private Context mContext;

        public boolean alertReady = false;

        public ThemedAlertDialog(View v, Context c) {

            mContext = c;

            adb = new AlertDialog.Builder(c);
            //et = new EditText(c);

            if (v != null) {

                v.getBackground().mutate().setColorFilter(c.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);

                adb.setView(v);
            }

            alertReady = false;
        }

        public void addBtns(String negBtn, String posBtn) {

            adb
                    .setNegativeButton(negBtn, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                        }
                    })
                    .setPositiveButton(posBtn, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
        }

        public void create() {

            ad = adb.create();
        }

        public void setBtnColor() {

            ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
            ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(mContext.getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
        }
    }

    public static class NameAlertDialog extends ThemedAlertDialog {

        public EditText et;

        public NameAlertDialog(String whatToCreate, String posBtn, String negBtn, String dfltTxt, View v, Context c) {

            super(v, c);

            et = (EditText) v;

            try {
                Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.set(v, planit.planit.home.planit003.R.drawable.et_cursor);
            } catch (Exception ignored) {
            }
            et.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setText(dfltTxt);

            super.addBtns(negBtn, posBtn);

            adb.setTitle("Enter " + whatToCreate.toLowerCase() + " name");
        }
    }
}
