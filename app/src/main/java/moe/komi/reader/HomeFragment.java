package moe.komi.reader;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moe.komi.crawler.Board;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

// Todo 首頁版面列表
public class HomeFragment extends Fragment {

    private ArrayList<Board> boards;
    private RecyclerView boardList;
    private BoardListAdapter boardListAdapter;

    public static final String ARG_BOARDS = "boards";

    public static HomeFragment newInstance(ArrayList<Board> boards) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BOARDS, boards);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boards = (ArrayList<Board>) getArguments().getSerializable(ARG_BOARDS);
        } else if (savedInstanceState != null) {
            boards = (ArrayList<Board>) savedInstanceState.getSerializable(ARG_BOARDS);
        } else {
            boards = new ArrayList<>();
        }
    }

    public static final int SPAN_COUNT = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        boardList = (RecyclerView) view.findViewById(R.id.board_list);
        boardList.setHasFixedSize(true);
        boardList.setLayoutManager(new GridLayoutManager(getActivity(), SPAN_COUNT));
        boardList.setAdapter(boardListAdapter = new BoardListAdapter());
        boardList.addItemDecoration(new SpacesItemDecoration((int) getResources().getDimension(R.dimen.board_list_item_space)));

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildPosition(view);

            switch (position % SPAN_COUNT) {
                case 0:
                case 1:
                    outRect.top = space;
                    outRect.left = space;
                    break;
                case 2:
                    outRect.top = space;
                    outRect.left = space;
                    outRect.right = space;
            }

            //Last Row
            if(boardListAdapter.getItemCount() - position <= SPAN_COUNT) {
                outRect.bottom = space;
            }
        }
    }

    private class BoardViewHolder extends RecyclerView.ViewHolder {

        private Board board;

        public BoardViewHolder(View itemView) {
            super(itemView);
        }

        public void setBoard(Board _board) {
            board = _board;

            ViewGroup boardlistItem = (ViewGroup) itemView;
            TextView itemName = (TextView) boardlistItem.findViewById(R.id.board_list_item_name);
            itemName.setText(board.name);

            boardlistItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new MainActivity.SelectBoardEvent(board));
                }
            });
        }
    }

    private class BoardListAdapter extends RecyclerView.Adapter<BoardViewHolder> {

        @Override
        public BoardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ViewGroup boardlistItem = (ViewGroup) inflater.inflate(R.layout.view_boardlist_item, null);
            return new BoardViewHolder(boardlistItem);
        }

        @Override
        public void onBindViewHolder(BoardViewHolder holder, int position) {
            holder.setBoard(boards.get(position));
        }

        @Override
        public int getItemCount() {
            return boards == null ? 0 : boards.size();
        }
    }
}
