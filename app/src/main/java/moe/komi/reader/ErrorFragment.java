package moe.komi.reader;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_error)
public class ErrorFragment extends Fragment {

    public static final String TAG = "ErrorFragment";

    public final static int ERROR_DEFAULT = 0;
    public final static int ERROR_GALLERY = 1;
    public final static int ERROR_BOARD = 2;

    private OnFragmentInteractionListener mListener;

    @FragmentArg
    public int type = ERROR_DEFAULT;

    @FragmentArg
    public String message = "";

    @ViewById(R.id.fragment_error_alert_text)
    public TextView alertTextView;

    @AfterViews
    void updateAlertText() {
        alertTextView.setText(message);
    }

    @Click(R.id.fragment_error_refresh_button)
    public void onRefreshButtonPressed(View view) {
        if (mListener != null) {
            mListener.onErrorFragmentRefreshButtonPressed(type);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GA();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onErrorFragmentRefreshButtonPressed(int type);
    }

    protected void GA() {
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(String.format("%s - %s", TAG, message));
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
