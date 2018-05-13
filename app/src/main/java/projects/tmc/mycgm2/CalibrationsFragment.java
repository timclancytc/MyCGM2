package projects.tmc.mycgm2;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import projects.tmc.mycgm2.exampleclasses.ProfileActivity;

public class CalibrationsFragment extends Fragment {
    private static final String CALIBRATIONS_URL = "https://api.dexcom.com/v1/users/self/calibrations";
    private static final String OAUTH_ACCESS_TOKEN_PARAM = "oauth2_access_token";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;

    private static final String TAG = "CalibrationsFragment";
    private RecyclerView mRecyclerView;
    private List<CalibrationItem> mItems = new ArrayList<>();

    public static CalibrationsFragment newInstance() {
        return new CalibrationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //Request basic profile of the use
        SharedPreferences preferences =
                this.getActivity().getSharedPreferences("user_info", 0);
        String accessToken = preferences.getString("accessToken", null);
        if (accessToken != null) {
            String calibrationsURL = getCalibrationsURL();
            Request calibrationsRequest = getCalibrationsRequest(calibrationsURL, accessToken);
            new GetCalibrationRequestAsyncTask().execute(calibrationsRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibrations, container, false);

        mRecyclerView = view.findViewById(R.id.calibration_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupAdapter();

        return view;
    }

    private Request getCalibrationsRequest(String calibrationsURL, String accessToken) {
        return new Request.Builder()
                .url(calibrationsURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new CalibrationAdapter(mItems));
        }
    }

    private class CalibrationHolder extends RecyclerView.ViewHolder {
        private TextView mItemTextView;

        public CalibrationHolder(View itemView) {
            super(itemView);
            mItemTextView = itemView.findViewById(R.id.calibration_text_view);
        }

        public void bind(CalibrationItem calibrationItem) {
            mItemTextView.setText(Float.toString(calibrationItem.getValue()));
        }
    }

    private class CalibrationAdapter extends RecyclerView.Adapter<CalibrationHolder> {
        private List<CalibrationItem> mCalibrationItems;

        public CalibrationAdapter(List<CalibrationItem> calibrationItems) {
            mCalibrationItems = calibrationItems;
        }

        @NonNull
        @Override
        public CalibrationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_calibration, parent, false);
            return new CalibrationHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull CalibrationHolder holder, int position) {
            holder.bind(mCalibrationItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mCalibrationItems.size();
        }
    }

    private static final String getCalibrationsURL() {
        String startDate = "2018-03-01T08:00:00";
        String endDate = "2018-05-12T08:00:00";
        return CALIBRATIONS_URL +
                QUESTION_MARK +
                "startDate=" + startDate +
                AMPERSAND +
                "endDate=" + endDate;
    }

    private class GetCalibrationRequestAsyncTask extends AsyncTask<Request, Void, List<CalibrationItem>> {

        @Override
        protected void onPreExecute() {
//            pd = ProgressDialog.show(CalibrationsFragment.this, "",
//                    CalibrationsFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<CalibrationItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new CalibrationFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<CalibrationItem> calibrationItems) {
            mItems = calibrationItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }




    }

    ;
}


