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

public class EGVSFragment extends Fragment {
    private static final String EGVS_URL = "https://api.dexcom.com/v1/users/self/egvs";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;

    private RecyclerView mRecyclerView;
    private List<EGVSItem> mItems = new ArrayList<>();

    public static EGVSFragment newInstance() {
        return new EGVSFragment();
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
            String egvsURL = getEGVSURL();
            Request egvsRequest = getEGVSRequest(egvsURL, accessToken);
            new GetEGVSRequestAsyncTask().execute(egvsRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_egvs, container, false);

        mRecyclerView = view.findViewById(R.id.egvs_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        setupAdapter();

        return view;
    }

    private Request getEGVSRequest(String egvsURL, String accessToken) {
        return new Request.Builder()
                .url(egvsURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new EGVSAdapter(mItems));
        }
    }

    private class EGVSHolder extends RecyclerView.ViewHolder {
        private final TextView mEGVSValueTextView;
        private final TextView mEGVSDateTextView;
        private final TextView mEGVSStatus;

        EGVSHolder(View itemView) {
            super(itemView);
            mEGVSValueTextView = itemView.findViewById(R.id.egvs_value_text_view);
            mEGVSDateTextView = itemView.findViewById(R.id.egvs_date_text_view);
            mEGVSStatus = itemView.findViewById(R.id.egvs_stagus);
        }

        void bind(EGVSItem egvsItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mEGVSValueTextView.setText(String.valueOf(egvsItem.getValue()));
            mEGVSDateTextView.setText(dateFormat.format(egvsItem.getSystemTime()));
            mEGVSStatus.setText(egvsItem.getStatus());
        }
    }

    private class EGVSAdapter extends RecyclerView.Adapter<EGVSHolder> {
        private final List<EGVSItem> mEGVSItems;

        EGVSAdapter(List<EGVSItem> egvsItems) {
            mEGVSItems = egvsItems;
        }

        @NonNull
        @Override
        public EGVSHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_egvs, parent, false);
            return new EGVSHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull EGVSHolder holder, int position) {
            holder.bind(mEGVSItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mEGVSItems.size();
        }
    }

    private String getEGVSURL() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date endDate = new Date();
        String startDateString = sf.format(new Date(cal.getTimeInMillis()));
        String endDateString = sf.format(endDate);
        return  EGVS_URL +
                QUESTION_MARK +
                "startDate=" + startDateString +
                AMPERSAND +
                "endDate=" + endDateString;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetEGVSRequestAsyncTask extends AsyncTask<Request, Void, List<EGVSItem>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(), "",
                    EGVSFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<EGVSItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new EGVSFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EGVSItem> egvsItems) {
            mItems = egvsItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }
    }
}


