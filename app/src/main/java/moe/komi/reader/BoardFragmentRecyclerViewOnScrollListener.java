package moe.komi.reader;

import android.support.v7.widget.LinearLayoutManager;

import de.greenrobot.event.EventBus;

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
