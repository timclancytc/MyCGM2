package projects.tmc.mycgm2;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import projects.tmc.mycgm2.Fetchers.CalibrationFetcher;
import projects.tmc.mycgm2.Fetchers.EGVSFetcher;
import projects.tmc.mycgm2.Fetchers.EventFetcher;
import projects.tmc.mycgm2.Items.CalibrationItem;
import projects.tmc.mycgm2.Items.EGVSItem;
import projects.tmc.mycgm2.Items.EventItem;

public class MyCGMLab {
    private List<EventItem> mEventItems;
    private List<CalibrationItem> mCalibrationItems;
    private List<EGVSItem> mEGVSItems;
    private ProgressDialog pd;

    private static MyCGMLab sMyCGMLab;


    public static MyCGMLab get() {
        if (sMyCGMLab == null) {
            sMyCGMLab = new MyCGMLab();
        }

        return sMyCGMLab;
    }

    private MyCGMLab() {
        mEventItems = new ArrayList<>();
        mCalibrationItems = new ArrayList<>();
        mEGVSItems = new ArrayList<>();
    }

    public interface OnRefreshItemsListener {
        void onRefreshItems(List<EventItem> eventItems,
                            List<CalibrationItem> calibrationItems,
                            List<EGVSItem> egvsItems);
    }

    private class FetchEventItemsTask extends AsyncTask<Request, Void, List<EventItem>> {
        private OnRefreshItemsListener mListener;

        public FetchEventItemsTask(OnRefreshItemsListener listener) {
            mListener = listener;
        }

        //TODO Enable Progress Dialog
//        @Override
//        protected void onPreExecute() {
//            pd = ProgressDialog.show(getActivity(), "",
//                    this.getString(R.string.loading), true);
//        }

        @Override
        protected List<EventItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new EventFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EventItem> eventItems) {
            mEventItems = eventItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            mListener.onRefreshItems(mEventItems, mCalibrationItems, mEGVSItems);
        }
    }

    private class FetchCalibrationItemsTask extends AsyncTask<Request, Void, List<CalibrationItem>> {
        private OnRefreshItemsListener mListener;

        //TODO Enable Progress Dialog
//        @Override
//        protected void onPreExecute() {
//            pd = ProgressDialog.show(getActivity(), "",
//                    this.getString(R.string.loading), true);
//        }

        public FetchCalibrationItemsTask(OnRefreshItemsListener listener) {
            mListener = listener;
        }

        @Override
        protected List<CalibrationItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new CalibrationFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<CalibrationItem> calibrationItems) {
            mCalibrationItems = calibrationItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            mListener.onRefreshItems(mEventItems, mCalibrationItems, mEGVSItems);
        }
    }

    private class FetchEGVSItemsTask extends AsyncTask<Request, Void, List<EGVSItem>> {
        private OnRefreshItemsListener mListener;

        //TODO Enable Progress Dialog
//        @Override
//        protected void onPreExecute() {
//            pd = ProgressDialog.show(getActivity(), "",
//                    this.getString(R.string.loading), true);
//        }

        public FetchEGVSItemsTask(OnRefreshItemsListener listener) {
            mListener = listener;
        }

        @Override
        protected List<EGVSItem> doInBackground(Request... requests) {
            Request request = requests[0];
            return new EGVSFetcher().fetchItems(request);
        }

        @Override
        protected void onPostExecute(List<EGVSItem> egvsItems) {
            mEGVSItems = egvsItems;
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            mListener.onRefreshItems(mEventItems, mCalibrationItems, mEGVSItems);
        }
    }

    public boolean hasEventItems() {
        if (mEventItems == null || mEventItems.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean hasCalibrationItems() {
        if (mCalibrationItems == null || mCalibrationItems.isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean hasEGVSItems() {
        if (mEGVSItems == null || mEGVSItems.isEmpty()) {
            return false;
        }

        return true;
    }

    public List<EventItem> getEventItems() {
        return mEventItems;
    }

    public EventItem getEventItem(int position) {
        return mEventItems.get(position);
    }

    public List<CalibrationItem> getCalibrationItems() {
        return mCalibrationItems;
    }

    public CalibrationItem getCalibrationItem(int position) {
        return mCalibrationItems.get(position);
    }

    public List<EGVSItem> getEGVSItems() {
        return mEGVSItems;
    }

    public EGVSItem getEGVSItem(int position) {
        return mEGVSItems.get(position);
    }

    public void refreshItems(OnRefreshItemsListener listener) {
        new FetchEventItemsTask(listener).execute();
        new FetchCalibrationItemsTask(listener).execute();
        new FetchEGVSItemsTask(listener).execute();
    }
}
