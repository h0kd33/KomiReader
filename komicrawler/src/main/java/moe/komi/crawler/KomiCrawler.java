package moe.komi.crawler;

import moe.komi.crawler.scraper.KomicaScraper;
import moe.komi.crawler.scraper.Scraper;
import moe.komi.crawler.scraper.ScraperAdapter;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

public class KomiCrawler implements Serializable {
    private ArrayList<moe.komi.crawler.Board> boards;

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

    public KomiCrawler(ArrayList<moe.komi.crawler.Board> boards) {
        this.boards = boards;
    }

    public ArrayList<moe.komi.crawler.Board> getBoards() {
        return boards;
    }

    public Scraper getScraper(moe.komi.crawler.Board board, ScraperAdapter scraperAdapter) throws KomiCrawlerException {
        switch(board.sid) {
            case KOMICA:
                return new KomicaScraper(board, scraperAdapter);
        }

        throw new KomiCrawlerException("Can't initlialize scraper for board: " + board.name);
    }
}
