package projects.tmc.mycgm2;

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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.Request;

public class InsulinFragment extends Fragment {
    private static final String EVENTS_URL = "https://api.dexcom.com/v1/users/self/events";
    private static final String QUESTION_MARK = "?";
    private static final String EQUALS = "=";
    private static final String AMPERSAND = "&";

    private ProgressDialog pd;

    private static final String TAG = "InsulinFragment";
    private RecyclerView mRecyclerView;
    private List<EventItem> mItems = new ArrayList<>();

    public static InsulinFragment newInstance() {
        return new InsulinFragment();
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
            String insulinURL = getInsulinURL();
            Request insulinRequest = getInsulinRequest(insulinURL, accessToken);
            new GetInsulinRequestAsyncTask().execute(insulinRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insulin, container, false);

        mRecyclerView = view.findViewById(R.id.insulin_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        setupAdapter();

        return view;
    }

    private Request getInsulinRequest(String insulinURL, String accessToken) {
        return new Request.Builder()
                .url(insulinURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new InsulinAdapter(mItems));
        }
    }

    private class InsulinHolder extends RecyclerView.ViewHolder {
        private final TextView mInsulinValueTextView;
        private final TextView mInsulinDateTextView;

        InsulinHolder(View itemView) {
            super(itemView);
            mInsulinValueTextView = itemView.findViewById(R.id.insulin_value_text_view);
            mInsulinDateTextView = itemView.findViewById(R.id.insulin_date_text_view);
        }

        void bind(EventItem eventItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mInsulinValueTextView.setText(Float.toString(eventItem.getValue()));
            mInsulinDateTextView.setText(dateFormat.format(eventItem.getSystemTime()));
        }
    }

    private class InsulinAdapter extends RecyclerView.Adapter<InsulinHolder> {
        private final List<EventItem> mEventItems;

        InsulinAdapter(List<EventItem> insulinItems) {
            mEventItems = insulinItems;
        }

        @NonNull
        @Override
        public InsulinHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_insulin, parent, false);
            return new InsulinHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull InsulinHolder holder, int position) {
            holder.bind(mEventItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mEventItems.size();
        }
    }

    private static String getInsulinURL() {
        String startDate = "2018-03-01T08:00:00";
        String endDate = "2018-05-12T08:00:00";
        return EVENTS_URL +
                QUESTION_MARK +
                "startDate=" + startDate +
                AMPERSAND +
                "endDate=" + endDate;
    }

    private class GetInsulinRequestAsyncTask extends AsyncTask<Request, Void, List<EventItem>> {

        @Override
        protected void onPreExecute() {
//            pd = ProgressDialog.show(InsulinFragment.this, "",
//                    InsulinFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<EventItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new InsulinFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EventItem> insulinItems) {
            mItems = insulinItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }
    }
}


