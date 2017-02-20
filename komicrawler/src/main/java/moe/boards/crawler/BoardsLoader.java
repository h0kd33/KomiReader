package moe.boards.crawler;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class BoardsLoader {
    public static ArrayList<Board> load(URL boardsJson) throws IOException {
        Gson gson = new Gson();
        return new ArrayList<Board>(Arrays.asList(gson.fromJson(
                Resources.toString(boardsJson, Charsets.UTF_8),
                Board[].class
        )));
    }

    public static ArrayList<Board> load(String boardsJson) {
        Gson gson = new Gson();
        return new ArrayList<Board>(Arrays.asList(gson.fromJson(
                boardsJson,
                Board[].class
        )));
    }

}
