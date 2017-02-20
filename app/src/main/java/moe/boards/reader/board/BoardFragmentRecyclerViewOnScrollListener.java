package moe.boards.reader.board;

import android.support.v7.widget.LinearLayoutManager;

import de.greenrobot.event.EventBus;
import moe.boards.reader.common.EndlessRecyclerOnScrollListener;

public class BoardFragmentRecyclerViewOnScrollListener extends EndlessRecyclerOnScrollListener {
    public BoardFragmentRecyclerViewOnScrollListener(LinearLayoutManager linearLayoutManager) {
        super(linearLayoutManager);
    }

    @Override
    public void onLoadMore() {
        EventBus.getDefault().post(new BoardFragment.LoadMoreEvent());
    }

    @Override
    public void onPageChanged(int page) {
        EventBus.getDefault().post(new PageChangedEvent(page));
    }

    public static final class PageChangedEvent {
        public PageChangedEvent(int page) {
            this.page = page;
        }

        public int page;
    }
}
