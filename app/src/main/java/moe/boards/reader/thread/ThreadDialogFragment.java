package moe.boards.reader.thread;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import moe.boards.crawler.Thread;
import moe.boards.crawler.scraper.Scraper;
import moe.boards.crawler.scraper.ScraperException;

import de.greenrobot.event.EventBus;
import moe.boards.reader.R;

public class ThreadDialogFragment extends DialogFragment {

    public static final String TAG = "ThreadDialog";

    public static final String ARG_RES = "res";
    public static final String ARG_SCRAPER = "scraper";

    private String res;
    private Scraper scraper;

    private View rootView;

    public static ThreadDialogFragment newInstance(Scraper scraper, String res) {
        ThreadDialogFragment threadDialogFragment = new ThreadDialogFragment();
        threadDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        Bundle args = new Bundle();
        args.putSerializable(ARG_RES, res);
        args.putSerializable(ARG_SCRAPER, scraper);
        threadDialogFragment.setArguments(args);
        return threadDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_thread, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // animation
        getDialog().getWindow().getAttributes().windowAnimations = R.style.ThreadDialogAnimation;

        Bundle args = getArguments();
        if (args != null) {
            res = args.getString(ARG_RES);
            scraper = (Scraper) args.getSerializable(ARG_SCRAPER);
        } else if (savedInstanceState != null) {
            res = savedInstanceState.getString(ARG_RES);
            scraper = (Scraper) savedInstanceState.getSerializable(ARG_SCRAPER);
        } else {
            dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkDestroyed()) {
            EventBus.getDefault().post(new LoadEvent(res));
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDetach() {
        res = null;
        scraper = null;
        rootView = null;
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ARG_RES, res);
        savedInstanceState.putSerializable(ARG_SCRAPER, scraper);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onEventAsync(LoadEvent event) {
        try {
            Thread thread = scraper.getFullThread(event.res);
            if (checkDestroyed()) {
                return;
            }

            EventBus.getDefault().post(new OnLoadEvent(thread));
        } catch (ScraperException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            showError(R.string.error_message_exception);
        }
    }

    public void onEventMainThread(OnLoadEvent event) {
        if (checkDestroyed()) {
            return;
        }

        ViewStub stubThread = (ViewStub) rootView.findViewById(R.id.stub_thread);

        if(stubThread == null) {
            Crashlytics.log("ThreadDialogFragment::OnLoadEvent : stubThread == null");
            return;
        }

        ThreadView threadView = (ThreadView) stubThread.inflate();
        threadView.setThread(event.thread);

        hideProgress();
    }

    private boolean checkDestroyed() {
        return res == null || scraper == null || rootView == null;
    }

    private void showError(int message) {
        if (checkDestroyed()) {
            return;
        }

        ViewStub errorStub = (ViewStub) rootView.findViewById(R.id.stub_error);
        ViewGroup errorView = (ViewGroup) errorStub.inflate();
        TextView errorText = (TextView) errorView.findViewById(R.id.fragment_error_alert_text);
        errorText.setText(getString(message));
        Button errorRefreshButton = (Button) errorView.findViewById(R.id.fragment_error_refresh_button);
        errorRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo Restart Thread Dialog
                //eventbus.post
            }
        });
    }

    private void hideProgress() {
        ((View) rootView.findViewById(R.id.thread_progress)).setVisibility(View.GONE);
    }

    public static class LoadEvent {
        public String res;

        public LoadEvent(String res) {
            this.res = res;
        }
    }

    public static class OnLoadEvent {
        public Thread thread;

        public OnLoadEvent(Thread thread) {
            this.thread = thread;
        }
    }

}
