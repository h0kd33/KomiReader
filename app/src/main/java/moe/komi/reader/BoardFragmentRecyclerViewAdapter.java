package moe.komi.reader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import moe.komi.crawler.Board;
import moe.komi.crawler.Post;
import moe.komi.crawler.Thread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoardFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "RecyclerViewAdapter";
    public static final int VIEWHOLDER_THREAD = 0;
    public static final int VIEWHOLDER_FOOTER = 1;

    private Boolean noMore = false;

    public void showNoMore() {
        noMore = true;
        notifyItemChanged(getItemCount() - 1);
    }

    public static class ThreadViewHolder extends RecyclerView.ViewHolder {

        public ThreadViewHolder(ThreadView view) {
            super(view);
            Log.d(TAG, "ThreadViewHolder::construct");
        }

        public void setThread(Thread thread) {
            ((ThreadView) this.itemView).setThread(thread);
        }

    }

    public static class BoardFooterViewHolder extends RecyclerView.ViewHolder {

        public BoardFooterView footerView;

        public BoardFooterViewHolder(BoardFooterView itemView) {
            super(itemView);
            Log.d(TAG, "BoardFooterViewHolder::construct");
            footerView = itemView;
            setIsRecyclable(false);
        }
    }

    private Context mContext;
    private RecyclerView mRecyclerView;
    private Board mBoard;

    private List<Thread> threads = new ArrayList<>();

    public BoardFragmentRecyclerViewAdapter(Context context, RecyclerView recyclerView, Board board) {
        mContext = context;
        mRecyclerView = recyclerView;
        mBoard = board;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        // Footer
        if (position == getItemCount() - 1) {
            return UUID.randomUUID().hashCode();
        }

        // Thread
        if (position < threads.size()) {
            return threads.get(position).uuid;
        }

        // Exception
        return -1;
    }

    public void addItem(Thread thread) {
        Log.i(TAG, String.format("BoardFragmentRecyclerViewAdapter::addItem() Thread No.%s", thread.posts.get(0).no));
        threads.add(thread);

        int position = threads.size() - 1;
        notifyItemInserted(position);

        // http://stackoverflow.com/questions/26860875/recyclerview-staggeredgridlayoutmanager-refrash-bug
        /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.invalidateItemDecorations();
            }
        }, 300);
        */
    }

    public void addAll(List<Thread> threads) {
        for (Thread thread : threads) {
            addItem(thread);
        }
    }

    public void removeItem(int position) {
        threads.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public int getItemViewType(int position) {
        if(position == getItemCount() - 1) {
            return VIEWHOLDER_FOOTER;
        } else {
            return VIEWHOLDER_THREAD;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEWHOLDER_THREAD: {
                Log.d(TAG, String.format("BoardFooterViewHolder::onCreateViewHolder VIEWHOLDER_THREAD"));
                final ThreadView view = (ThreadView) LayoutInflater.from(mContext).inflate(R.layout.view_thread, parent, false);
                ThreadViewHolder threadViewHolder = new ThreadViewHolder(view);
                return threadViewHolder;
            }
            case VIEWHOLDER_FOOTER: {
                final BoardFooterView view = (BoardFooterView) LayoutInflater.from(mContext).inflate(R.layout.view_board_footer, parent, false);
                return new BoardFooterViewHolder(view);
            }
            default:
                throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ThreadViewHolder) {
            ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
            threadViewHolder.setThread(threads.get(position));

            if(threads.size() > position && threads.get(position).posts.size() > 0) {
                Post firstPost = threads.get(position).posts.get(0);
                Log.d(TAG, String.format("BoardFooterViewHolder::onBindViewHolder %d => ThreadViewHolder No.%s", position, firstPost.no));
            } else {
                threadViewHolder.itemView.setVisibility(View.GONE);
            }
        } else if (holder instanceof BoardFooterViewHolder) {
            BoardFooterView view = (BoardFooterView) holder.itemView;
            if(noMore == false) {
                view.setNextPageButtonVisibility(View.VISIBLE);
                view.setNomoreVisibility(View.GONE);
            } else {
                view.setNextPageButtonVisibility(View.GONE);
                view.setNomoreVisibility(View.VISIBLE);
            }
            Log.d(TAG, "BoardFooterViewHolder::onBindViewHolder => BoardFooterViewHolder");
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "onAttachedToRecyclerView");
        //EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        Log.d(TAG, "onDetachedFromRecyclerView");
        //EventBus.getDefault().unregister(this);
    }

    private boolean isPositionFooter(int position) {
        return position == threads.size();
    }

    @Override
    public int getItemCount() {
        return threads.size() + 1; // size + footer
    }
}
