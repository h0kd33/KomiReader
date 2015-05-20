package moe.komi.reader;

import android.app.ProgressDialog;
import android.content.Context;

public class LoadingProgressFactory {

    public static ProgressDialog getInstance(Context context, int message) {
        ProgressDialog loadingProgressDialog = new ProgressDialog(context);
        loadingProgressDialog.setMessage(context.getString(message));
        loadingProgressDialog.setIndeterminate(true);
        loadingProgressDialog.setCancelable(false);
        loadingProgressDialog.setInverseBackgroundForced(true);
        loadingProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return loadingProgressDialog;
    }
}
