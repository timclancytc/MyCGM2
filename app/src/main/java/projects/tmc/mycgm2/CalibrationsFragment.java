package projects.tmc.mycgm2;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class CalibrationsFragment extends Fragment {
    private static final String CALIBRATIONS_URL = "https://api.linkedin.com/v1/people/~";
    private static final String OAUTH_ACCESS_TOKEN_PARAM ="oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";

    private RecyclerView mRecyclerView;
    private List<CalibrationItem> mCalibrationItems;
}
