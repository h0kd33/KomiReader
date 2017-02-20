package moe.boards.reader;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeDialogFragment extends DialogFragment {

    public static WelcomeDialogFragment newInstance() {
        WelcomeDialogFragment welcomeDialogFragment = new WelcomeDialogFragment();
        welcomeDialogFragment.setCancelable(false);
        welcomeDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        return welcomeDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

}
