package moe.boards.crawler.scraper;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import moe.boards.crawler.Board;
import moe.boards.crawler.Thread;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Scraper implements Serializable {

    public ArrayList<Thread> getCurrentPageThreads() {
        return currentPageThreads;
    }

    ArrayList<Thread> currentPageThreads = new ArrayList<>();

    public abstract ArrayList<Thread> getPage(String page) throws ScraperException;

    public abstract Thread getFullThread(String res) throws ScraperException;

    public Board board;

    public ScraperAdapter scraperAdapter;

    public Scraper(Board board, ScraperAdapter scraperAdapter) {
        System.out.println("Scraper(" + board.name + ") initialized");
        this.board = board;
        this.scraperAdapter = scraperAdapter;
    }

    protected boolean hasNext = false;
    protected String nextPageLink = null;

    public String getNextPageLink() {
        return nextPageLink;
    }

    public Integer getNextPageNumber() {
        if(nextPageLink == null) {
            return 0;
        } else {
            Pattern p = Pattern.compile("(\\d+).*");
            Matcher m = p.matcher(nextPageLink);
            if(m.find()) {
                return Integer.parseInt(m.group(1));
            } else {
                return 0;
            }
        }
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public ArrayList<Thread> getNextPage() throws ScraperException, ScraperNoMorePageException {
        if (isHasNext()) {
            currentPageThreads = getPage(nextPageLink);
            return currentPageThreads;
        } else {
            throw new ScraperNoMorePageException(board, " getNextPage when hasNext==false");
        }
    }

    public Board getBoard() {
        return board;
    }

}
