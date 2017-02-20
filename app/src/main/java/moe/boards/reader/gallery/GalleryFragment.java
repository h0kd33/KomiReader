package moe.boards.reader.gallery;


import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.koushikdutta.ion.Ion;

import de.greenrobot.event.EventBus;
import moe.boards.reader.MyApplication;
import moe.boards.reader.R;

//TODO 刪除圖片
public class GalleryFragment extends Fragment {

    public static final String TAG = "GalleryFragment";

    Cursor cursor;
    RecyclerView gallery;

    final public static class ScrollTopEvent {
    }

    public void onEventMainThread(ScrollTopEvent event) {
        if (gallery != null) {
            gallery.smoothScrollToPosition(0);
        }
    }

    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupGallery();

        GA_onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        gallery.removeAllViews();
        gallery = null;
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursor = null;
        }
        System.gc();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onPause();
    }

    private void setupGallery() {
        gallery = (RecyclerView) getView().findViewById(R.id.gallery_recyclerview);
        //gallery.setLayoutManager(new GridLayoutManager(getActivity().getApplicationContext(), 4));
        gallery.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        gallery.setAdapter(new CursorAdapter());
        //gallery.setHasFixedSize(true);
    }

    public void showImageDialog(String url, String mime) {
        // Remove previous created dialog
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = GalleryImageDialogFragment.newInstance(url, mime);
        newFragment.show(ft, "dialog");
    }

    private class CursorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public CursorAdapter() {
            getCursor();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView cardView = (CardView) LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.view_gallery_image, null);

            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(cardView) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(cursor != null) {
                if (cursor.moveToPosition(position)) {
                    CardView cardView = (CardView) holder.itemView;
                    ImageView imageView = (ImageView) cardView.findViewById(R.id.gallery_image_view);
                    final String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)),
                            mime = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
                    Ion.with(imageView)
                            .load(uri);

                    cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showImageDialog(uri, mime);
                            GA_galleryClickImage(uri);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            if(cursor == null) {
                return 0;
            } else {
                return cursor.getCount();
            }
        }

        // http://stackoverflow.com/a/22481379/670662
        private Cursor getCursor() {
            if (cursor == null) {
                //final String[] columns = {MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails.HEIGHT, MediaStore.Images.Thumbnails.WIDTH};
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns.MIME_TYPE};
                String path = "%/" + getString(R.string.download_path) + "/%";

                cursor = getActivity()
                        .getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                columns,
                                MediaStore.Images.Thumbnails.DATA + " like ? ",
                                new String[]{path},
                                MediaStore.MediaColumns.DATE_ADDED + " DESC");

                notifyDataSetChanged();

                return cursor;
            } else {
                return cursor;
            }
        }
    }

    private void GA_onResume() {
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void GA_galleryClickImage(String uri) {
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker();
        t.send(new HitBuilders.EventBuilder()
                .setCategory(getString(R.string.ga_galleryCategory))
                .setAction(getString(R.string.ga_galleryClickImage))
                .setLabel(uri)
                .build());
    }
}
