package moe.boards.crawler.scraper;

import moe.boards.crawler.Board;

public class ScraperException extends Exception {

    public ScraperException(Board board, String message) {
        super("Scraper=" + board.sid.getValue() + "; " + "Board=" + board.name + ": " + message);

    }
}
