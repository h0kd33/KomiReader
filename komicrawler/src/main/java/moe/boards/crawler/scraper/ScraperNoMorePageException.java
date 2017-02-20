package moe.boards.crawler.scraper;

import moe.boards.crawler.Board;

public class ScraperNoMorePageException extends ScraperException {
    public ScraperNoMorePageException(Board board, String message) {
        super(board, message);
    }
}
