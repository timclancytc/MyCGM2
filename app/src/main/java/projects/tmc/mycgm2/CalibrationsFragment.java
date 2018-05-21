package projects.tmc.mycgm2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.Request;
import projects.tmc.mycgm2.Fetchers.CalibrationFetcher;
import projects.tmc.mycgm2.Items.CalibrationItem;
import projects.tmc.mycgm2.Items.EGVSItem;
import projects.tmc.mycgm2.Items.EventItem;

public class CalibrationsFragment extends Fragment
        implements MyCGMLab.OnRefreshItemsListener {
    private static final String CALIBRATIONS_URL = "https://api.dexcom.com/v1/users/self/calibrations";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;

    private RecyclerView mRecyclerView;
    private MyCGMLab mMyCGMLab = MyCGMLab.get();
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
                Objects.requireNonNull(this.getActivity()).getSharedPreferences("user_info", 0);
        String accessToken = preferences.getString("accessToken", null);
        if (accessToken != null) {
            String calibrationsURL = getCalibrationsURL();
            Request calibrationsRequest = getCalibrationsRequest(calibrationsURL, accessToken);
            new GetCalibrationRequestAsyncTask().execute(calibrationsRequest);
        }

        if (!mMyCGMLab.hasCalibrationItems()) {
            mMyCGMLab.refreshItems(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibrations, container, false);

        mRecyclerView = view.findViewById(R.id.calibration_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
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
            mRecyclerView.setAdapter(new CalibrationAdapter(mMyCGMLab.getCalibrationItems()));
        }
    }

    @Override
    public void onRefreshItems(List<EventItem> eventItems,
                               List<CalibrationItem> calibrationItems,
                               List<EGVSItem> egvsItems) {
        setupAdapter();
    }

    private class CalibrationHolder extends RecyclerView.ViewHolder {
        private final TextView mCalibrationValueTextView;
        private final TextView mCalibrationDateTextView;

        CalibrationHolder(View itemView) {
            super(itemView);
            mCalibrationValueTextView = itemView.findViewById(R.id.calibration_value_text_view);
            mCalibrationDateTextView = itemView.findViewById(R.id.calibration_date_text_view);
        }

        void bind(CalibrationItem calibrationItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mCalibrationValueTextView.setText(String.valueOf(calibrationItem.getValue()));
            mCalibrationDateTextView.setText(dateFormat.format(calibrationItem.getSystemTime()));
        }
    }

    private class CalibrationAdapter extends RecyclerView.Adapter<CalibrationHolder> {
        private final List<CalibrationItem> mCalibrationItems;

        CalibrationAdapter(List<CalibrationItem> calibrationItems) {
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

    private String getCalibrationsURL() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date endDate = new Date();
        String startDateString = sf.format(new Date(cal.getTimeInMillis()));
        String endDateString = sf.format(endDate);
        return CALIBRATIONS_URL +
                QUESTION_MARK +
                "startDate=" + startDateString +
                AMPERSAND +
                "endDate=" + endDateString;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCalibrationRequestAsyncTask extends AsyncTask<Request, Void, List<CalibrationItem>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(), "",
                    CalibrationsFragment.this.getString(R.string.loading), true);
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
}


