package moe.boards.reader;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.SubscriberExceptionEvent;
import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;
import moe.boards.crawler.Board;
import moe.boards.crawler.KomiCrawler;
import moe.boards.crawler.KomiCrawlerException;
import moe.boards.reader.board.BoardFragment;
import moe.boards.reader.gallery.GalleryFragment;

public class MainActivity
        extends Activity
        implements ErrorFragment.OnFragmentInteractionListener {

    public static final String TAG = "MainActivity";

    public static final int DRAWER_HOME = 0;
    public static final int DRAWER_GALLERY = 1;

    private Drawer.Result drawerResult;
    private int drawerSelectedPosition = 0;
    private FrameLayout frameContainer;
    private Fragment contentFragment;
    private ArrayList<Board> boards;
    private HashMap<Board, IDrawerItem> boardDrawerItem = new HashMap<>();

    /**
     * Event Flow:
     * MainActivity::onStart => StartLoadingEvent => LoadingEvent => FinishedLoadingEvent
     */
    public static class StartLoadingEvent {
    }

    public static class LoadingEvent {
    }

    public static class FinishedLoadingEvent {
        public Throwable error = null;
    }

    public static class SelectBoardEvent {
        public Board board;

        public SelectBoardEvent(Board board) {
            this.board = board;
        }
    }

    public static class DownloadPostImageEvent {
        public String url;

        public DownloadPostImageEvent(String url) {
            this.url = url;
        }
    }

    public static class ToastEvent {
        public String message;

        public ToastEvent(String message) {
            this.message = message;
        }
    }

    public static class ErrorEvent {
        public Fragment targetFragment;
        public String message;
        public Throwable throwable;

        public ErrorEvent(String message, Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
        }

        public ErrorEvent(Fragment targetFragment, String message, Throwable throwable) {
            this(message, throwable);
            this.targetFragment = targetFragment;
        }
    }

    @DebugLog
    public void onEvent(SubscriberExceptionEvent event) {
        EventBus.getDefault().post(new ErrorEvent(getString(R.string.error_message_exception), event.throwable));
    }

    @DebugLog
    public void onEventMainThread(ErrorEvent event) {
        int type = ErrorFragment.ERROR_DEFAULT;
        if (contentFragment != null) {
            if (contentFragment instanceof GalleryFragment) {
                type = ErrorFragment.ERROR_GALLERY;
            } else if (contentFragment instanceof BoardFragment) {
                type = ErrorFragment.ERROR_BOARD;
            }
        }
        showErrorFragment(type, event.message);

        if (event.throwable != null) {
            Crashlytics.logException(event.throwable);
        }
    }

    @DebugLog
    public void onEventBackgroundThread(DownloadPostImageEvent event) {
        Uri uri = Uri.parse(event.url);
        String filename = uri.getLastPathSegment();
        File downloadPath = getDownloadPath(filename);

        if (downloadPath.exists()) {
            EventBus.getDefault().post(new ToastEvent(String.format(getString(R.string.postimage_downloadede_message), filename)));
        } else {
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDestinationUri(Uri.fromFile(downloadPath));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

            downloadManager.enqueue(request);

            EventBus.getDefault().post(new ToastEvent(String.format(getString(R.string.postimage_download_message), filename)));
        }
    }

    public void onEventMainThread(ToastEvent event) {
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
    }

    public File getDownloadPath(String filename) {
        return new File(getDownloadDir() + File.separator + filename);
    }

    public File getDownloadDir() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + getString(R.string.download_path));
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    @DebugLog
    public void onEventMainThread(StartLoadingEvent event) {
        showLoadingDialog();
        EventBus.getDefault().post(new LoadingEvent());
    }

    @DebugLog
    public void onEventAsync(LoadingEvent event) {
        FinishedLoadingEvent finishedLoadingEvent = new FinishedLoadingEvent();

        try {
            komiCrawler = new KomiCrawler(new URL(getString(R.string.boards_json_url)));
        } catch (Exception e) {
            e.printStackTrace();
            finishedLoadingEvent.error = e;
        }

        EventBus.getDefault().post(finishedLoadingEvent);
    }

    @DebugLog
    public void onEventMainThread(FinishedLoadingEvent event) {
        if (event.error != null) {
            if (event.error instanceof MalformedURLException) {
                Crashlytics.logException((MalformedURLException) event.error);
            } else if (event.error instanceof KomiCrawlerException) {
                Crashlytics.logException((KomiCrawlerException) event.error);
            } else {
                Crashlytics.logException(event.error);
            }

            showErrorFragment(ErrorFragment.ERROR_DEFAULT, getString(R.string.error_message_exception));
        } else {
            if (checkDestroyed()) {
                boards = komiCrawler.getBoards();

                // Create board sections
                for (Board board : boards) {
                    int icon = R.drawable.ic_komica;

                    IDrawerItem drawerItem = new PrimaryDrawerItem()
                            .withName(board.name)
                            .withIcon(icon)
                            .withTag(board)
                            .withIdentifier(board.id.intValue());
                    drawerResult.addItem(drawerItem);

                    boardDrawerItem.put(board, drawerItem);
                }

                // Restore selected drawer item
                if (drawerSelectedPosition > 0 && drawerSelectedPosition < drawerResult.getDrawerItems().size()) {
                    drawerResult.setSelection(drawerSelectedPosition, false);
                } else {
                    drawerResult.setSelection(drawerSelectedPosition = DRAWER_HOME);
                }
            }
        }

        hideLoadingDialog();
    }

    private boolean checkDestroyed() {
        return boards == null;
    }

    @DebugLog
    public void onEventMainThread(BoardFragment.RefreshEvent event) {
        setFragment(BoardFragment.newInstance(event.board));
    }

    public void onEventMainThread(SelectBoardEvent event) {
        setFragment(BoardFragment.newInstance(event.board));
        mToolbar.setLogo(R.drawable.ic_komica);
        mToolbar.setTitle(event.board.name);
        mToolbar.inflateMenu(R.menu.board_menu);

        if (drawerResult != null && boardDrawerItem.containsKey(event.board)) {
            drawerResult.setSelection(boardDrawerItem.get(event.board), false);
        }
    }

    @DebugLog
    public void onEvent(BoardFragment.InitEvent event) {
        //post back with KomiCrawler
        EventBus.getDefault().post(new BoardFragment.InitResultEvent(komiCrawler));
    }

    KomiCrawler komiCrawler = null;

    public Toolbar getToolbar() {
        return mToolbar;
    }

    Toolbar mToolbar;

    private ProgressDialog loadingProgressDialog;

    @DebugLog
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup Fabric
        final Fabric fabric = new Fabric.Builder(this)
                .kits(Crashlytics.getInstance())
                .build();
        Fabric.with(fabric);

        // setContentView
        setContentView(R.layout.activity_main);

        // container for fragment
        frameContainer = (FrameLayout) findViewById(R.id.frame_container);

        // setup toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setClipToPadding(true);
        mToolbar.setLogo(R.mipmap.ic_launcher);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setNavigationIcon(R.drawable.ic_view_headline_white_48dp);
        mToolbar.setCollapsible(true);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new BoardFragment.ScrollTopEvent());
                EventBus.getDefault().post(new GalleryFragment.ScrollTopEvent());
            }
        });

        if (drawerResult == null) {
            // create navigation drawer
            drawerResult = new Drawer()
                    .withActivity(this)
                    .withToolbar(mToolbar)
                    .withTranslucentStatusBar(true)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l, IDrawerItem iDrawerItem) {
                            drawerSelectedPosition = i;

                            Menu toolbarMenu = mToolbar.getMenu();
                            if (toolbarMenu != null) {
                                toolbarMenu.clear();
                            }

                            switch (i) {
                                case 0:
                                    if (!connected) {
                                        showNotConnected();
                                    } else {
                                        setFragment(HomeFragment.newInstance(boards));
                                        mToolbar.setLogo(R.drawable.ic_launcher);
                                        mToolbar.setTitle(getString(R.string.app_name));
                                        mToolbar.inflateMenu(R.menu.main_menu);
                                    }
                                    return;
                                case 1:
                                    setFragment(GalleryFragment.newInstance());
                                    mToolbar.setLogo(R.drawable.ic_launcher);
                                    mToolbar.setTitle(getString(R.string.downlaod_gallery));
                                    mToolbar.inflateMenu(R.menu.main_menu);
                                    return;
                            }

                            // Check board
                            Object tag = iDrawerItem.getTag();
                            if (tag instanceof Board) {
                                if (!connected) {
                                    showNotConnected();
                                } else {
                                    EventBus.getDefault().post(new SelectBoardEvent((Board) tag));
                                }
                            }
                        }
                    })
                    .addDrawerItems(
                            new PrimaryDrawerItem().withName(R.string.app_name).withIcon(R.drawable.ic_home_black_48dp),
                            new PrimaryDrawerItem().withName(R.string.downlaod_gallery).withIcon(R.drawable.ic_folder_multiple_image_black_48dp),
                            new SectionDrawerItem().withName(R.string.boards_list)
                    )
                    .build();
        } else if (drawerResult.getDrawerItems() != null) {
            drawerResult.removeAllItems();
        }
    }

    private void setFragment(Fragment fragment) {
        if (contentFragment != null) {
            getFragmentManager().beginTransaction().remove(contentFragment).commit();
        }

        contentFragment = fragment;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

    /**
     * Always go home
     */
    @Override
    public void onBackPressed() {
        if (contentFragment != null && !(contentFragment instanceof HomeFragment)) {
            drawerResult.setSelection(DRAWER_HOME);
        } else {
            super.onBackPressed();
        }
    }

    @DebugLog
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @DebugLog
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Serializable crawler = savedInstanceState.getSerializable(ARG_CRAWLER);
            if (crawler != null) {
                komiCrawler = (KomiCrawler) crawler;
            }

            int drawerSelectedPosition = savedInstanceState.getInt(ARG_DRAERDPOSITION);
            if (drawerSelectedPosition >= 0) {
                this.drawerSelectedPosition = drawerSelectedPosition;
            }
        }
    }

    private boolean connected = false;

    public boolean isConnected() {
        return connected;
    }

    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();

        if (!checkConnection()) {
            connected = false;
            showNotConnected();
        } else {
            connected = true;
            if (komiCrawler == null) {
                //Start Loading Boards
                EventBus.getDefault().post(new StartLoadingEvent());
            } else {
                //Restored
                EventBus.getDefault().post(new FinishedLoadingEvent());
            }
        }
    }

    private void showNotConnected() {
        EventBus.getDefault().post(new ErrorEvent(getString(R.string.error_message_not_connected), null));
    }

    @DebugLog
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        boards = null;
        boardDrawerItem = null;
    }

    /*
    // TODO main_menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    */

    WelcomeDialogFragment welcomeDialogFragment;

    @DebugLog
    public void showLoadingDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("welcome");
        if (prev != null) {
            ft.remove(prev);
        }

        welcomeDialogFragment = WelcomeDialogFragment.newInstance();
        welcomeDialogFragment.show(ft, "welcome");

        /*
        loadingProgressDialog = LoadingProgressFactory.getInstance(this, R.string.loading_boards);
        loadingProgressDialog.show();
        */
    }

    @DebugLog
    public void hideLoadingDialog() {
        if (welcomeDialogFragment != null) {
            welcomeDialogFragment.dismiss();
        }
        /*
        if (loadingProgressDialog != null) {
            loadingProgressDialog.cancel();
        }
        */
    }

    @DebugLog
    private void showErrorFragment(int type, String message) {
        hideLoadingDialog();
        mToolbar.setTitle(getString(R.string.error_message_title));
        setFragment(
                ErrorFragment_.builder()
                        .type(type)
                        .message(message)
                        .build()
        );
    }

    //todo 版面錯誤處理
    @DebugLog
    @Override
    public void onErrorFragmentRefreshButtonPressed(int type) {
        switch (type) {
            case ErrorFragment.ERROR_GALLERY:
                drawerResult.setSelection(DRAWER_GALLERY);
                break;
            default:
                resetActivity();
        }
    }

    @DebugLog
    public void resetActivity() {
        // http://wangshifuola.blogspot.tw/2011/10/androidactivity-activityname-has-leaked.html
        hideLoadingDialog();
        EventBus.getDefault().unregister(this);

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public static final String ARG_CRAWLER = "crawler";
    public static final String ARG_DRAERDPOSITION = "drawer";
    public static final String ARG_CONTENTFRAGMENT = "content";

    @DebugLog
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ARG_CRAWLER, komiCrawler);
        outState.putInt(ARG_DRAERDPOSITION, drawerSelectedPosition);
        super.onSaveInstanceState(outState);
    }

    @DebugLog
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
        Ion.getDefault(getApplicationContext()).getBitmapCache().clear();
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = cm.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }

    private void GA_boardScrollTop() {
        Tracker t = ((MyApplication) getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.ga_boardCategory))
                .setAction(getString(R.string.ga_boardScrollTop))
                .setLabel("")
                .build());
    }

    private void GA_galleryScrollTop() {
        Tracker t = ((MyApplication) getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.ga_galleryCategory))
                .setAction(getString(R.string.ga_galleryScrollTop))
                .setLabel("")
                .build());
    }

}
