package moe.boards.reader.thread;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;

import de.greenrobot.event.EventBus;
import moe.boards.reader.MainActivity;
import moe.boards.reader.MyApplication;
import moe.boards.reader.R;

public class PostImageDialogFragment extends DialogFragment {

    public static final String TAG = "PostImageDialog";

    //public PhotoView imageView;
    //public SubsamplingScaleImageView imageView;
    public WebView imageWebView;

    public ProgressWheel progressWheel;

    public ImageButton postImageDownloadButton;

    public static final String ARG_URL = "url";
    String url = null;

    public void afterViews() {
        if (url == null) {
            Log.d(TAG, "afterViews => url=null");
            progressWheel.setVisibility(View.GONE);
            //imageView.setImageDrawable(getResources().getDrawable(R.drawable.img_error));
            error();
        } else {
            Log.d(TAG, "afterViews => Ion.load " + url);

            System.gc();
            /*
            Ion.with(imageView)
                    //.placeholder(R.drawable.img_placeholder_250x250)
                    //.deepZoom()
                    .smartSize(false)
                    .error(R.drawable.img_error)
                    .load(url)
                    .setCallback(new FutureCallback<ImageView>() {
                        @Override
                        public void onCompleted(Exception e, ImageView result) {
                            setupMenu();
                            progressWheel.setVisibility(View.GONE);
                            Log.d(TAG, "Ion.with => onCompleted");
                        }
                   });
            */
            Glide.with(getActivity()).load(url).downloadOnly(new SimpleTarget<File>() {
                @Override
                public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                    Log.d(TAG, "Loaded: " + resource.getAbsolutePath());

                    if(imageWebView != null) {
                        imageWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
                        imageWebView.getSettings().setBuiltInZoomControls(true);
                        imageWebView.getSettings().setSupportZoom(true);
                        imageWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        imageWebView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                progressWheel.setVisibility(View.GONE);
                                setupMenu();
                            }
                        });
                        String html = "<html><head>" +
                                "<meta name=\"viewport\" content=\"width=device-width,height=device-height\" />\n" +
                                "<style>" +
                                "body { width: 100%; height: 100%; margin: 0; padding:0; } img { display:block; margin: auto; width: 100%;}" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "<img src='" + resource.getAbsolutePath() + "' />" +
                                "</body></html>";
                        imageWebView.loadDataWithBaseURL("file://", html, "text/html", "UTF-8", null);
                        //imageWebView.loadUrl(Uri.fromFile(resource).toString());
                    }
                }
            });
        }
    }

    private void error() {
        //imageView.setImage(ImageSource.resource(R.drawable.img_error));
    }

    private void setupMenu() {
        postImageDownloadButton.setVisibility(View.VISIBLE);
        postImageDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postImageDownloadButton.setVisibility(View.GONE);
                EventBus.getDefault().post(new MainActivity.DownloadPostImageEvent(url));

                GA_postImageDownloadButtonOnClick(url);
            }
        });
    }

    static public PostImageDialogFragment newInstance(String url) {
        PostImageDialogFragment f = new PostImageDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || savedInstanceState.getString(ARG_URL) == null) {
            this.url = getArguments().getString(ARG_URL);
        } else {
            this.url = savedInstanceState.getString(ARG_URL);
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.FullScreenDialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_post_image, container, false);
        //imageView = (SubsamplingScaleImageView) v.findViewById(R.id.post_full_image);
        imageWebView = (WebView) v.findViewById(R.id.post_full_image_webview);
        progressWheel = (ProgressWheel) v.findViewById(R.id.image_progress);
        postImageDownloadButton = (ImageButton) v.findViewById(R.id.action_postimage_download);
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
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //imageView = null;
        imageWebView = null;
        progressWheel = null;
        postImageDownloadButton = null;
        System.gc();
    }

    private void GA_postImageDownloadButtonOnClick(String url) {
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getResources().getString(R.string.ga_postCategory))
                .setAction(getResources().getString(R.string.ga_postDownloadImage))
                .setLabel(url)
                .build());
    }

}
