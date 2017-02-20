package moe.boards.reader.board;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import moe.boards.reader.R;

public class BoardFooterView extends RelativeLayout {

    public TextView nomoreTextView;
    public Button nextPageButton;

    @Override
    public void onFinishInflate() {
        nomoreTextView = (TextView) findViewById(R.id.nomore_text);

        nextPageButton = (Button) findViewById(R.id.next_page_button);
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new BoardFragment.LoadMoreEvent());
            }
        });
    }

    public void setNomoreVisibility(int visibility) {
        if (nomoreTextView != null) {
            nomoreTextView.setVisibility(visibility);
        }
    }

    public void setNextPageButtonVisibility(int visibility) {
        if(nextPageButton != null) {
            nextPageButton.setVisibility(visibility);
        }
    }

    public BoardFooterView(Context context) {
        super(context);
        init();
    }

    public BoardFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

}
