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

@SuppressWarnings("unused")
public class EventsFragment extends Fragment {
    private static final String EVENT_URL = "https://api.dexcom.com/v1/users/self/events";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";

    @SuppressWarnings("unused")
    private ProgressDialog pd;

    private RecyclerView mRecyclerView;
    private List<EventItem> mItems = new ArrayList<>();

    @SuppressWarnings("unused")
    public static EventsFragment newInstance() {
        return new EventsFragment();
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
            String eventsURL = getEventsURL();
            Request eventsRequest = getEventsRequest(eventsURL, accessToken);
            new GetEventRequestAsyncTask().execute(eventsRequest);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        mRecyclerView = view.findViewById(R.id.event_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        setupAdapter();

        return view;
    }

    private Request getEventsRequest(String eventsURL, String accessToken) {
        return new Request.Builder()
                .url(eventsURL)
                .get()
                .addHeader("authorization", "Bearer " + accessToken)
                .build();
    }


    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new EventAdapter(mItems));
        }
    }

    private class EventHolder extends RecyclerView.ViewHolder {
        private final TextView mEventValueTextView;
        private final TextView mEventDateTextView;

        @SuppressWarnings("unused")
        EventHolder(View itemView) {
            super(itemView);
            mEventValueTextView = itemView.findViewById(R.id.event_value_text_view);
            mEventDateTextView = itemView.findViewById(R.id.event_date_text_view);
        }

        void bind(EventItem eventItem) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'  'HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            mEventValueTextView.setText(String.valueOf(eventItem.getValue()));
            mEventDateTextView.setText(dateFormat.format(eventItem.getSystemTime()));
        }
    }

    private class EventAdapter extends RecyclerView.Adapter<EventHolder> {
        private final List<EventItem> mEventItems;

        EventAdapter(List<EventItem> eventItems) {
            mEventItems = eventItems;
        }

        @NonNull
        @Override
        public EventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_event, parent, false);
            return new EventHolder(view);

        }

        @Override
        public void onBindViewHolder(@NonNull EventHolder holder, int position) {
            holder.bind(mEventItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mEventItems.size();
        }
    }

    private String getEventsURL() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -90);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Date endDate = new Date();
        String startDateString = sf.format(new Date(cal.getTimeInMillis()));
        String endDateString = sf.format(endDate);
        return EVENT_URL +
                QUESTION_MARK +
                "startDate=" + startDateString +
                AMPERSAND +
                "endDate=" + endDateString;
    }

    @SuppressWarnings("unused")
    @SuppressLint("StaticFieldLeak")
    private class GetEventRequestAsyncTask extends AsyncTask<Request, Void, List<EventItem>> {

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(getActivity(), "",
                    EventsFragment.this.getString(R.string.loading), true);
        }

        @Override
        protected List<EventItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new EventFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EventItem> eventItems) {
            mItems = eventItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            setupAdapter();
        }
    }
}


