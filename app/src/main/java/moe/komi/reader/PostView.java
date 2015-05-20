package moe.komi.reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import moe.komi.crawler.Post;

import de.greenrobot.event.EventBus;
import uk.co.deanwild.flowtextview.FlowTextView;
import uk.co.deanwild.flowtextview.listeners.OnLinkClickListener;

public class PostView extends RelativeLayout {

    protected FlowTextView flowtext;
    protected ImageView postImage;
    protected TextView postNo;
    protected TextView postId;
    protected TextView postDate;

    public PostView(Context context) {
        super(context);
        init(context);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    protected void onFinishInflate() {
        flowtext = (FlowTextView) findViewById(R.id.flowtext);
        postImage = (ImageView) findViewById(R.id.post_image);
        postNo = (TextView) findViewById(R.id.post_no);
        postId = (TextView) findViewById(R.id.post_id);
        postDate = (TextView) findViewById(R.id.post_date);
    }

    public void setPost(Post post) {
        setText(post);
        setImage(post);
        setStatus(post);
    }

    protected void setStatus(Post post) {
        postNo.setText(String.format(getResources().getString(R.string.post_no), post.no));
        postId.setText(String.format(getResources().getString(R.string.post_id), post.tripId));
        postDate.setText(getPostRelativeTime(post));
    }

    protected String getPostRelativeTime(Post post) {
        return DateUtils.getRelativeTimeSpanString(post.date.getTimeInMillis()).toString();
    }

    protected void setText(Post post) {
        if (post.com != null) {
            flowtext.setColor(getResources().getColor(R.color.post_text_color));
            flowtext.setTextSize(getResources().getDimension(R.dimen.post_textsize) * getResources().getConfiguration().fontScale);
            flowtext.setText(Html.fromHtml(post.com + "     " /** 避免FlowTextView把字截掉 **/));
            flowtext.setOnLinkClickListener(new OnLinkClickListener() {
                @Override
                public void onLinkClick(String url) {
                    onUrlClick(url);
                }
            });
        }
    }

    protected void onUrlClick(String url) {
        playSoundEffect(SoundEffectConstants.CLICK);

        if (url.matches(".+?\\.(jpg|gif|png|jpeg|bmp)$")) {
            showImageDialog(url);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            getContext().startActivity(browserIntent);
        }
    }

    protected void setImage(Post post) {
        if (post.image != null) {
            loadImage(post.image.th, post.image.src);
        } else {
            if (postImage != null) {
                postImage.setVisibility(GONE);
            }
        }
    }

    protected void loadImage(String thumb, String src) {
        FutureCallback<ImageView> callback = new PostImageOnLoadFutureCallback(src);
        Ion.with(postImage)
                .error(R.drawable.img_error)
                .load(thumb)
                .setCallback(callback);

        postImage.setOnClickListener(new PostImageOnClickListener(src));
    }

    public void showImageDialog(String url) {
        EventBus.getDefault().post(new BoardFragment.ShowImageEvent(url));
    }

    private class PostImageOnClickListener implements OnClickListener {

        String url;

        private PostImageOnClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View view) {
            showImageDialog(url);
            //GA_postImageOnClick(url);
        }
    }

    protected class PostImageOnLoadFutureCallback implements FutureCallback<ImageView> {

        String url;

        protected PostImageOnLoadFutureCallback(String url) {
            this.url = url;
        }

        @Override
        public void onCompleted(Exception e, ImageView result) {
            postImage.setOnClickListener(new PostImageOnClickListener(url));

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flowtext.invalidate();
                }
            });
        }
    }

    private void GA_postImageOnClick(String url) {
        Tracker t = ((MyApplication) ((Activity) getContext()).getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getResources().getString(R.string.ga_postCategory))
                .setAction(getResources().getString(R.string.ga_postClickImage))
                .setLabel(url)
                .build());
    }

}
