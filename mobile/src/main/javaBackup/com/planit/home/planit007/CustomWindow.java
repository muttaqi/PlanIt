package planit007.planit.home.planit007;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.planit.home.planit003.R;

/**
 * Created by Home on 2018-07-26.
 */

public class CustomWindow extends Activity {
    protected TextView title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        setContentView(planit.planit.home.planit003.R.layout.activity_main);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, planit.planit.home.planit003.R.layout.window_title);

        title = (TextView) findViewById(planit.planit.home.planit003.R.id.title);
        title.setText("PlanIt");
    }
}
