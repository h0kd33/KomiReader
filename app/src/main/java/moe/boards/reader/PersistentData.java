package moe.boards.reader;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import moe.boards.crawler.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Utility class for saving and restoring persistent data like a list of favorites */
/** https://github.com/eugenkiss/chanobol/blob/master/src/main/java/anabolicandroids/chanobol/ui/scaffolding/PersistentData.java **/
public class PersistentData {
    public static final String favoriteBoardsId = "favoriteBoards";

    SharedPreferences prefs;
    Gson gson = new Gson();
    ArrayList<FavoritesCallback> favoritesCallbacks = new ArrayList<>();

    public PersistentData(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    private void saveFavorites(Set<Board> favorites) {
        String jsonFavorites = gson.toJson(favorites);
        prefs.edit().putString(favoriteBoardsId, jsonFavorites).apply();
        for (FavoritesCallback cb : favoritesCallbacks) {
            cb.onChanged(favorites);
        }
    }

    public void addFavorite(Board board) {
        Set<Board> favorites = getFavorites();
        favorites.add(board);
        saveFavorites(favorites);
    }

    public void removeFavorite(Board board) {
        Set<Board> favorites = getFavorites();
        favorites.remove(board);
        saveFavorites(favorites);
    }

    public Set<Board> getFavorites() {
        String jsonFavorites = prefs.getString(favoriteBoardsId, "[]");
        Board[] boards;
        try {
            boards = gson.fromJson(jsonFavorites, Board[].class);
        } catch(JsonSyntaxException e){
            boards = new Board[0];
        }
        return new HashSet<>(Arrays.asList(boards));
    }

    public void addFavoritesChangedCallback(FavoritesCallback callback) {
        favoritesCallbacks.add(callback);
    }

    public void removeFavoritesChangedCallback(FavoritesCallback callback) {
        favoritesCallbacks.remove(callback);
    }

    public static interface FavoritesCallback {
        public void onChanged(Set<Board> newFavorites);
    }

}