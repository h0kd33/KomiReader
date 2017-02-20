package moe.boards.crawler;

import moe.boards.crawler.scraper.KomicaScraper;
import moe.boards.crawler.scraper.Scraper;
import moe.boards.crawler.scraper.ScraperAdapter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

public class KomiCrawler implements Serializable {
    private ArrayList<moe.boards.crawler.Board> boards;

    public KomiCrawler(URL boardsJson) throws KomiCrawlerException {
        try {
            boards = BoardsLoader.load(boardsJson);
        } catch (IOException e) {
            throw new KomiCrawlerException("Boards loading failed from: " + boardsJson.toString());
        }
    }

    public KomiCrawler(String boardsJson) {
        boards = BoardsLoader.load(boardsJson);
    }

    public KomiCrawler(ArrayList<moe.boards.crawler.Board> boards) {
        this.boards = boards;
    }

    public ArrayList<moe.boards.crawler.Board> getBoards() {
        return boards;
    }

    public Scraper getScraper(moe.boards.crawler.Board board, ScraperAdapter scraperAdapter) throws KomiCrawlerException {
        switch(board.sid) {
            case KOMICA:
                return new KomicaScraper(board, scraperAdapter);
        }

        throw new KomiCrawlerException("Can't initlialize scraper for board: " + board.name);
    }
}
