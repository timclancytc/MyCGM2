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

public class CarbFragment extends Fragment {
    private static final String EVENTS_URL = "https://api.dexcom.com/v1/users/self/events";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;

    private RecyclerView mRecyclerView;
    private List<EventItem> mItems = new ArrayList<>();

    public static CarbFragment newInstance() {
        return new CarbFragment();
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
            String carbURL = getCarbURL();
            Request carbRequest = getCarbRequest(carbURL, accessToken);
            new GetCarbRequestAsyncTask().execute(carbRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carb, container, false);

        mRecyclerView = view.findViewById(R.id.carb_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        setupAdapter();

        return view;
    }

    private Request getCarbRequest(String carbURL, String accessToken) {
        return new Request.Builder()
                .url(carbURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new CarbAdapter(mItems));
        }
    }

    private class CarbHolder extends RecyclerView.ViewHolder {
        private final TextView mCarbValueTextView;
        private final TextView mCarbDateTextView;

        CarbHolder(View itemView) {
            super(itemView);
            mCarbValueTextView = itemView.findViewById(R.id.carb_value_text_view);
            mCarbDateTextView = itemView.findViewById(R.id.carb_date_text_view);
        }

        void bind(EventItem eventItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mCarbValueTextView.setText(String.valueOf(eventItem.getValue()));
            mCarbDateTextView.setText(dateFormat.format(eventItem.getSystemTime()));
        }
    }

    private class CarbAdapter extends RecyclerView.Adapter<CarbHolder> {
        private final List<EventItem> mEventItems;

        CarbAdapter(List<EventItem> carbItems) {
            mEventItems = carbItems;
        }

        @NonNull
        @Override
        public CarbHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_carb, parent, false);
            return new CarbHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull CarbHolder holder, int position) {
            holder.bind(mEventItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mEventItems.size();
        }
    }

    private String getCarbURL() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date endDate = new Date();
        String startDateString = sf.format(new Date(cal.getTimeInMillis()));
        String endDateString = sf.format(endDate);
        return  EVENTS_URL +
                QUESTION_MARK +
                "startDate=" + startDateString +
                AMPERSAND +
                "endDate=" + endDateString;
    }

    @SuppressLint("StaticFieldLeak")
    private class GetCarbRequestAsyncTask extends AsyncTask<Request, Void, List<EventItem>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(), "",
                    CarbFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<EventItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new CarbFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EventItem> carbItems) {
            mItems = carbItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }
    }
}


