package moe.boards.reader.thread;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import moe.boards.crawler.Post;
import moe.boards.crawler.Thread;

import de.greenrobot.event.EventBus;
import moe.boards.reader.R;
import moe.boards.reader.board.BoardFragment;

public class ThreadView extends CardView {

    public static final String TAG = "ThreadView";

    public LinearLayout postsList;

    private String res;

    public ThreadView(Context context) {
        super(context);
    }

    public ThreadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThreadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onFinishInflate() {
        postsList = (LinearLayout) findViewById(R.id.posts_list);
    }

    public void setThread(Thread thread) {
        createPostViews(thread);
    }

    public void createPostViews(Thread thread) {
        postsList.removeAllViews();

        int i = 0;
        for (Post post : thread.posts) {
            PostView postView;

            if (i == 0) {
                postView = (PostViewFirst) getLayoutInflater().inflate(R.layout.view_post_first, null);
                ((PostViewFirst) postView).setThread(thread);

                postsList.addView(postView);
                postView.setPost(post);
                res = post.no;

                Log.d(TAG, String.format("Add Divider No.%s ignoredNumber=%d", post.no, thread.ignoredNumber));
                if (thread.ignoredNumber > 0) {
                    LinearLayout threadReminderView = (LinearLayout) getLayoutInflater().inflate(R.layout.view_thread_reminder, null);
                    TextView threadReminderText = (TextView) threadReminderView.findViewById(R.id.thread_reminder_text);
                    threadReminderText.setText(String.format(getResources().getString(R.string.thread_reminder_text), thread.ignoredNumber));
                    threadReminderView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EventBus.getDefault().post(new BoardFragment.ShowThreadEvent(res));
                        }
                    });
                    postsList.addView(threadReminderView);
                }
            } else {
                Log.d(TAG, String.format("Add postView No.%s", post.no));
                postView = (PostView) getLayoutInflater().inflate(R.layout.view_post, null);

                //Set background color
                if (i % 2 == 0) {
                    postView.setBackgroundColor(getResources().getColor(R.color.reply_back_even));
                } else {
                    postView.setBackgroundColor(getResources().getColor(R.color.reply_back_odd));
                }

                postView.setPost(post);
                postsList.addView(postView);
            }

            i++;
        }
    }

    private LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}
