package moe.boards.reader.common;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int scrollState = RecyclerView.SCROLL_STATE_IDLE;
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 0; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private ArrayList<Integer> pageAnchors = new ArrayList<>();

    private int current_page = 0;

    private LinearLayoutManager mLinearLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        Log.d(TAG, String.format("onScrolled: totalItemCount = %d, firstVisibleItem = %d, loading = %d, scrollState = %d, dx = %d, dy = %d", totalItemCount, firstVisibleItem, loading ? 1 : 0, scrollState, dx, dy));

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;

                pageAnchors.add(totalItemCount);

                previousTotal = totalItemCount;
            }
        }

        if(scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached

                onLoadMore();

                loading = true;
            }

            //Check current page
            int page = Collections.binarySearch(pageAnchors, firstVisibleItem);
            page = page < 0 ? -(page + 1) : (page + 1);
            if (page != current_page) {
                onPageChanged(current_page = page);
            }
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        Log.d(TAG, String.format("onScrollStateChanged %d", newState));
        scrollState = newState;
    }

    abstract public void onLoadMore();

    abstract public void onPageChanged(int page);
}
