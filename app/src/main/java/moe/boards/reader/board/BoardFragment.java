package moe.boards.reader.board;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.Serializable;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import moe.boards.crawler.Board;
import moe.boards.crawler.KomiCrawler;
import moe.boards.crawler.KomiCrawlerException;
import moe.boards.crawler.Thread;
import moe.boards.crawler.scraper.Scraper;
import moe.boards.crawler.scraper.ScraperException;
import moe.boards.reader.MainActivity;
import moe.boards.reader.MyApplication;
import moe.boards.reader.OkHttpScraperAdapter;
import moe.boards.reader.thread.PostImageDialogFragment;
import moe.boards.reader.R;
import moe.boards.reader.thread.ThreadDialogFragment;

public class BoardFragment extends Fragment {

    public static Boolean loading = false;

    public static final String TAG = "BoardFragment";

    public Toolbar toolbar;

    public ProgressWheel boardProgress;
    public RelativeLayout boardProgressLayout;
    public TextView boardProgressString;
    public String[] loadingStrings;

    public RecyclerView threadsListView;

    public BoardLayoutManager boardLayoutManager;

    private BoardFragmentRecyclerViewAdapter threadsListAdapater;

    public Board board;

    Scraper mScraper = null;

    public KomiCrawler komiCrawler;

    final public static String ARG_BOARD = "board";
    final public static String ARG_SCRAPER = "scraper";
    final public static String ARG_KOMICRAWLER = "komiCrawler";

    /**
     * Event Flow
     * InitEvent => MainActivity => InitResultEvent => BoardFragment => LoadMoreEvent(first page)
     */
    static final public class InitEvent {
        public InitEvent() {
            Log.d(TAG, "BoardFragment.InitEvent::construct");
        }
    }

    static final public class InitResultEvent {
        public KomiCrawler komiCrawler;

        public InitResultEvent(KomiCrawler komiCrawler) {
            Log.d(TAG, "BoardFragment.InitResultEvent::construct");
            this.komiCrawler = komiCrawler;
        }
    }

    static final public class RefreshEvent {
        public RefreshEvent(Board board) {
            this.board = board;
        }

        public Board board;

        public RefreshEvent() {
            Log.d(TAG, "BoardFragment.RefreshEvent::construct");
        }
    }

    static final public class LoadMoreEvent {
        public LoadMoreEvent() {
            Log.d(TAG, "BoardFragment.LoadMoreEvent::construct");
        }
    }

    static final public class LoadMoreResultEvent {
        public boolean hasThreads() {
            return threads != null && threads.size() > 0;
        }

        public ArrayList<Thread> threads;

        public boolean isInit() {
            return init;
        }

        protected boolean init = false;

        public boolean isHasNext() {
            return hasNext;
        }

        protected boolean hasNext = true;

        public LoadMoreResultEvent(ArrayList<Thread> threads, boolean init, boolean hasNext) {
            this.threads = threads;
            this.init = init;
            this.hasNext = hasNext;
        }
    }

    static final public class ScrollTopEvent {
        public ScrollTopEvent() {
        }
    }

    static final public class ShowLoadingEvent {
    }

    static final public class HideLoadingEvent {
    }

    static final public class PageChangedEvent {
        public int page;

        public PageChangedEvent(int page) {
            this.page = page;
        }
    }

    static final public class ShowImageEvent {
        public String url;

        public ShowImageEvent(String url) {
            this.url = url;
        }
    }

    static final public class ShowThreadEvent {
        public String res;

        public ShowThreadEvent(String res) {
            this.res = res;
        }
    }

    /**
     * 從MainActivity取得KomiCrawler
     *
     * @param event
     */
    public void onEventMainThread(InitResultEvent event) {
        Log.d(TAG, "BoardFragment::onEvent(BoardFragment.InitResultEvent)");
        komiCrawler = event.komiCrawler;

        if (threadsListView == null) {
            Crashlytics.log("BoardFragment::InitResultEvent : threadsListView == null");
            return;
        }

        //Load first page
        mScraper = null;
        EventBus.getDefault().post(new LoadMoreEvent());
        EventBus.getDefault().post(new PageChangedEvent(0));
    }

    private void createThreadsListAdapter() {
        threadsListAdapater = new BoardFragmentRecyclerViewAdapter(getActivity(), threadsListView, board);
        threadsListView.setAdapter(threadsListAdapater);
        threadsListAdapater.notifyDataSetChanged();
    }

    public void onEventAsync(LoadMoreEvent event) {
        Log.d(TAG, "BoardFragment::LoadMoreEvent");

        if(board == null) {
            Crashlytics.log("BoardFragment::LoadMoreEvent : board == null");
            return;
        }

        loading = true;

        if (mScraper == null) {
            EventBus.getDefault().post(new ShowLoadingEvent());

            try {
                mScraper = komiCrawler.getScraper(board, new OkHttpScraperAdapter());
                mScraper.getPage(null);

                // BoardFragment destroyed => don't send result event
                if (checkDestroyed()) {
                    return;
                } else {
                    EventBus.getDefault().post(new HideLoadingEvent());
                    EventBus.getDefault().post(new LoadMoreResultEvent(mScraper.getCurrentPageThreads(), true, mScraper.isHasNext()));
                }
            } catch (KomiCrawlerException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                showError(e);
            } catch (ScraperException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                showError(e);
            }
        } else if (mScraper.isHasNext()) {
            try {
                EventBus.getDefault().post(new PageChangedEvent(mScraper.getNextPageNumber()));
                EventBus.getDefault().post(new ShowLoadingEvent());
                mScraper.getNextPage();
                if (checkDestroyed()) {
                    //destroyed
                    //讀取完成前就離開
                    return;
                } else {
                    EventBus.getDefault().post(new HideLoadingEvent());
                    EventBus.getDefault().post(new LoadMoreResultEvent(mScraper.getCurrentPageThreads(), false, mScraper.isHasNext()));
                }
            } catch (ScraperException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                showError(e);
            }
        } else {
            EventBus.getDefault().post(new LoadMoreResultEvent(null, false, false));
        }
    }

    public void onEventMainThread(ShowLoadingEvent event) {
        if (boardProgressLayout == null) {
            Crashlytics.log("BoardFragment::ShowLoadingEvent: boardProgressLayout == null");
            return;
        }

        boardProgressLayout.setVisibility(View.VISIBLE);
        int i = (int) Math.round(Math.random() * loadingStrings.length);
        i = i == loadingStrings.length ? i - 1 : i;
        boardProgressString.setText(loadingStrings[i]);

        threadsListView.setVisibility(View.GONE);
    }

    public void onEventMainThread(HideLoadingEvent event) {
        if (boardProgressLayout == null) {
            Crashlytics.log("BoardFragment::HideLoadingEvent: boardProgressLayout == null");
            return;
        }

        if (threadsListView == null) {
            Crashlytics.log("BoardFragment::HideLoadingEvent: threadsListView == null");
            return;
        }

        boardProgressLayout.setVisibility(View.GONE);
        threadsListView.setVisibility(View.VISIBLE);
    }

    public void onEventMainThread(LoadMoreResultEvent event) {
        if (threadsListView == null) {
            Crashlytics.log("BoardFragment::LoadMoreResultEvent: threadsListView == null");
            return;
        }

        Log.d(TAG, "BoardFragment::onEvent(BoardFragment.LoadMoreResultEvent)");
        loading = false;

        if (event.hasThreads()) {
            createThreadsListAdapter();
            threadsListAdapater.addAll(event.threads);
            threadsListAdapater.notifyDataSetChanged();
            threadsListView.scrollToPosition(0);
        } else if (threadsListAdapater != null) {
            threadsListAdapater.showNoMore();
        } else {
            Crashlytics.log("BoardFragment::LoadMoreResultEvent: threadsListAdapater == null");
        }
    }

    public void onEventMainThread(ScrollTopEvent event) {
        scrollTop();
    }

    public void scrollTop() {
        if (threadsListView == null) {
            Crashlytics.log("BoardFragment::scrollTop: threadsListView == null");
            return;
        }

        threadsListView.smoothScrollToPosition(0);
    }

    public void onEventMainThread(ShowThreadEvent event) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(ThreadDialogFragment.TAG);
        if (prev == null) {
            ft.addToBackStack(null);
            ThreadDialogFragment threadDialogFragment = ThreadDialogFragment.newInstance(mScraper, event.res);
            threadDialogFragment.show(ft, ThreadDialogFragment.TAG);
        }
    }

    public void onEventMainThread(ShowImageEvent event) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(PostImageDialogFragment.TAG);
        if (prev == null) {
            ft.addToBackStack(null);
            PostImageDialogFragment postImageDialogFragment = PostImageDialogFragment.newInstance(event.url);
            postImageDialogFragment.show(ft, PostImageDialogFragment.TAG);
        }
    }

    //todo Swipe to refresh
    public void onRefresh() {
        Log.i(TAG, "BoardFragment::onRefresh");
        EventBus.getDefault().post(new RefreshEvent(board));
    }

    public static BoardFragment newInstance(Board board) {
        BoardFragment boardFragment = new BoardFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BoardFragment.ARG_BOARD, board);
        boardFragment.setArguments(bundle);
        return boardFragment;
    }

    private BoardMenuItemClickListener onMenuItemClick = new BoardMenuItemClickListener(this);

    private class BoardMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        private BoardFragment boardFragment;

        public BoardMenuItemClickListener(BoardFragment boardFragment) {
            this.boardFragment = boardFragment;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_refresh:
                    onRefresh();
                    GA_toolBarMenuRefresh();
                    break;
            }
            return true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "BoardFragment::onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BoardFragment::onCreate");

        // handle fragment arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            handleArguments(arguments);
        }

        if (savedInstanceState != null) {
            handleSavedInstanceState(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "BoardFragment::onCreateView");

        // restore saved state
        if (savedInstanceState != null) {
            handleSavedInstanceState(savedInstanceState);
        }

        // setup options menu
        setHasOptionsMenu(true);

        //Inflate fragment view
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        //Get progressWheel
        boardProgress = (ProgressWheel) view.findViewById(R.id.board_progress);
        boardProgressString = (TextView) view.findViewById(R.id.loading_string);
        boardProgressLayout = (RelativeLayout) view.findViewById(R.id.board_progress_layout);
        loadingStrings = getResources().getStringArray(R.array.loading_strings);

        //Get threadsListView
        boardLayoutManager = new BoardLayoutManager(getActivity());
        threadsListView = (RecyclerView) view.findViewById(R.id.threads_list);
        threadsListView.setLayoutManager(boardLayoutManager);
        //threadsListView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        threadsListView.setHasFixedSize(true);
        threadsListView.setItemViewCacheSize(5);

        return view;
    }

    public void onEventMainThread(PageChangedEvent event) {
        if(toolbar == null) {
            Crashlytics.log("BoardFragment::PageChangedEvent : toolbar == null");
            return;
        }

        if(board == null) {
            Crashlytics.log("BoardFragment::PageChangedEvent : board == null");
            return;
        }

        toolbar.setTitle(String.format(getString(R.string.page_title), board.name, event.page));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "BoardFragment::onActivityCreated");

        if (savedInstanceState != null) {
            handleSavedInstanceState(savedInstanceState);
        }

        toolbar = ((MainActivity) getActivity()).getToolbar();
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "BoardFragment::onStart");
        super.onStart();
        EventBus.getDefault().register(this);

        if (checkRestored()) {
            if (checkDestroyed()) {
                EventBus.getDefault().post(new InitResultEvent(komiCrawler));
            } else {
                //Do nothing
            }
        } else {
            EventBus.getDefault().post(new InitEvent());
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "BoardFragment::onResume");
        super.onResume();

        GA_onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "BoardFragment::onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "BoardFragment::onStop");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "BoardFragment::onDestroyView");
        super.onDestroyView();

        //cleanup
        toolbar = null;
        mScraper = null;
        threadsListView = null;
        boardLayoutManager = null;
        threadsListAdapater = null;
        board = null;
        komiCrawler = null;
        System.gc();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "BoardFragment::onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "BoardFragment::onDetach");
        super.onDetach();
    }

    private void handleArguments(Bundle arguments) {
        Serializable arg_board = arguments.getSerializable(ARG_BOARD);
        if (arg_board != null) {
            board = (Board) arg_board;
        }
    }

    private void handleSavedInstanceState(Bundle savedInstanceState) {
        Serializable arg_board = savedInstanceState.getSerializable(ARG_BOARD),
                arg_scraper = savedInstanceState.getSerializable(ARG_SCRAPER),
                arg_komicrawler = savedInstanceState.getSerializable(ARG_KOMICRAWLER);

        if (arg_board != null) {
            board = (Board) arg_board;
        }

        if (arg_scraper != null) {
            mScraper = (Scraper) arg_scraper;
        }

        if (arg_komicrawler != null) {
            komiCrawler = (KomiCrawler) arg_komicrawler;
        }
    }

    private Boolean checkRestored() {
        return board != null && mScraper != null && komiCrawler != null;
    }

    private Boolean checkDestroyed() {
        return !isAdded() || threadsListView == null || komiCrawler == null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "BoardFragment::onSaveInstanceState");
        outState.putSerializable(ARG_BOARD, board);
        outState.putSerializable(ARG_SCRAPER, mScraper);
        outState.putSerializable(ARG_KOMICRAWLER, komiCrawler);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onTrimMemory(int state) {
        Log.d(TAG, "onTrimMemory");
        Ion.getDefault(getActivity().getApplicationContext()).getBitmapCache().clear();
    }

    private void showError(Throwable e) {
        if (isAdded() && mScraper != null) {
            EventBus.getDefault().post(new MainActivity.ErrorEvent(getString(R.string.error_message_exception), e));
        }
    }

    private void GA_onResume() {
        if (board != null) {
            Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
            t.setScreenName(String.format("%s - %s", TAG, board.name));
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    private void GA_toolBarMenuRefresh() {
        if (board != null) {
            Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
            t.send(new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.ga_boardCategory))
                    .setAction(getString(R.string.ga_boardToolbarRefresh))
                    .setLabel(board.name)
                    .build());
        }
    }
}
