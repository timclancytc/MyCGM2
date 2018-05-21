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
import projects.tmc.mycgm2.Fetchers.HealthFetcher;
import projects.tmc.mycgm2.Items.EventItem;

public class HealthFragment extends Fragment {
    private static final String EVENTS_URL = "https://api.dexcom.com/v1/users/self/events";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;
    private RecyclerView mRecyclerView;
    private List<EventItem> mItems = new ArrayList<>();

    public static HealthFragment newInstance() {
        return new HealthFragment();
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
            String healthURL = getHealthURL();
            Request healthRequest = getHealthRequest(healthURL, accessToken);
            new GetHealthRequestAsyncTask().execute(healthRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health, container, false);

        mRecyclerView = view.findViewById(R.id.health_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        setupAdapter();

        return view;
    }

    private Request getHealthRequest(String healthURL, String accessToken) {
        return new Request.Builder()
                .url(healthURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new HealthAdapter(mItems));
        }
    }

    private class HealthHolder extends RecyclerView.ViewHolder {
        private final TextView mHealthValueTextView;
        private final TextView mHealthDateTextView;

        HealthHolder(View itemView) {
            super(itemView);
            mHealthValueTextView = itemView.findViewById(R.id.health_value_text_view);
            mHealthDateTextView = itemView.findViewById(R.id.health_date_text_view);
        }

        void bind(EventItem eventItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mHealthValueTextView.setText(String.valueOf(eventItem.getValue()));
            mHealthDateTextView.setText(dateFormat.format(eventItem.getSystemTime()));
        }
    }

    private class HealthAdapter extends RecyclerView.Adapter<HealthHolder> {
        private final List<EventItem> mEventItems;

        HealthAdapter(List<EventItem> healthItems) {
            mEventItems = healthItems;
        }

        @NonNull
        @Override
        public HealthHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_health, parent, false);
            return new HealthHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull HealthHolder holder, int position) {
            holder.bind(mEventItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mEventItems.size();
        }
    }

    private String getHealthURL() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date endDate = new Date();
        String startDateString = sf.format(new Date(cal.getTimeInMillis()));
        String endDateString = sf.format(endDate);
        return EVENTS_URL +
                QUESTION_MARK +
                "startDate=" + startDateString +
                AMPERSAND +
                "endDate=" + endDateString;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetHealthRequestAsyncTask extends AsyncTask<Request, Void, List<EventItem>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(), "",
                    HealthFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<EventItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new HealthFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EventItem> healthItems) {
            mItems = healthItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }
    }
}


