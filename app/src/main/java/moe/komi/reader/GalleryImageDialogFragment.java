package moe.komi.reader;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.pnikosis.materialishprogress.ProgressWheel;

import uk.co.senab.photoview.PhotoView;

//TODO Merge with PostImageDialogFragment
public class GalleryImageDialogFragment extends DialogFragment {

    public static final String TAG = "GalleryImageDialog";

    public PhotoView imageView;

    public ProgressWheel progressWheel;

    public ImageButton galleryImageDeleteButton;

    public static final String ARG_URL = "url";
    public static final String ARG_MIME = "mime";

    String url = null;
    String mime = "";

    public void afterViews() {
        if (url == null) {
            progressWheel.setVisibility(View.GONE);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.img_error));
        } else {
            FutureCallback<ImageView> callback = new FutureCallback<ImageView>() {
                @Override
                public void onCompleted(Exception e, ImageView result) {
                    progressWheel.setVisibility(View.GONE);
                    //setupMenu();

                    if (e != null) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }
            };

            // todo https://github.com/koush/ion/issues/499
            if (isGif()) {
                System.gc();
                Ion.with(imageView)
                        .crossfade(true)
                        .error(R.drawable.img_error)
                        .load(url)
                        .setCallback(callback);
            } else {
                System.gc();
                Ion.with(imageView)
                        .crossfade(true)
                        .error(R.drawable.img_error)
                        .deepZoom()
                        .load(url)
                        .setCallback(callback);
            }
        }

        GA();
    }

    private boolean isGif() {
        return mime.contains("gif") || mime.contains("GIF");
    }

    /*
    //Todo 刪除圖片
    private void setupMenu() {
        galleryImageDeleteButton.setVisibility(View.VISIBLE);
        galleryImageDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryImageDeleteButton.setVisibility(View.GONE);
                EventBus.getDefault().post(new MainActivity.DownloadPostImageEvent(url));
            }
        });
    }
    */

    static GalleryImageDialogFragment newInstance(String url, String mime) {
        GalleryImageDialogFragment f = new GalleryImageDialogFragment();

        // Save arguem
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_MIME, mime);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || savedInstanceState.getString(ARG_URL) == null) {
            url = getArguments().getString(ARG_URL);
            mime = getArguments().getString(ARG_MIME);
        } else {
            url = savedInstanceState.getString(ARG_URL);
            mime = savedInstanceState.getString(ARG_MIME);
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_gallery_image, container, false);
        imageView = (PhotoView) v.findViewById(R.id.gallery_full_image);
        progressWheel = (ProgressWheel) v.findViewById(R.id.image_progress);
        //galleryImageDeleteButton = (ImageButton) v.findViewById(R.id.action_postimage_download);
        afterViews();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // animation
        getDialog().getWindow().getAttributes().windowAnimations = R.style.ImageDialogAnimation;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ARG_URL, url);
        savedInstanceState.putString(ARG_MIME, mime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imageView = null;
        progressWheel = null;
        System.gc();
    }

    protected void GA() {
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

}
