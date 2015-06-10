package moe.komi.reader.board;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class BoardLayoutManager extends LinearLayoutManager {
    public BoardLayoutManager(Context context) {
        super(context);
        init();
    }

    public BoardLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    private void init() {
        setSmoothScrollbarEnabled(false);
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return 600;
    }

}
