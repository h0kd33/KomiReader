package moe.komi.reader.thread;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import moe.komi.crawler.*;

import moe.komi.reader.R;
import uk.co.deanwild.flowtextview.FlowTextView;

public class PostViewFirst extends PostView {

    public PostViewFirst(Context context) {
        super(context);
    }

    public PostViewFirst(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostViewFirst(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        flowtext = (FlowTextView) findViewById(R.id.flowtext);
        postDate = (TextView) findViewById(R.id.post_date);
        postId = (TextView) findViewById(R.id.post_id);
    }

    protected void setThread(moe.komi.crawler.Thread thread) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Post post = thread.posts.get(0);

        LinearLayout sll;
        if(post.image == null) {
            sll = (LinearLayout) inflater.inflate(R.layout.tpl_post_header_noimg, null);
        } else {
            sll = (LinearLayout) inflater.inflate(R.layout.tpl_post_header, null);
            postImage = (ImageView) sll.findViewById(R.id.post_image);
        }

        postNo = (TextView) sll.findViewById(R.id.post_no);
        TextView postCount = (TextView) sll.findViewById(R.id.post_count);
        postCount.setText(String.format(getResources().getString(R.string.post_count), thread.getReplyCount()));

        FrameLayout postHeaderContainer = (FrameLayout) findViewById(R.id.post_header_container);
        postHeaderContainer.addView(sll);
    }

    @Override
    protected void setImage(Post post) {
        if (post.image != null) {
            loadImage(post.image.src, post.image.src);
        } else {
            if (postImage != null) {
                postImage.setVisibility(GONE);
            }
        }
    }

}
