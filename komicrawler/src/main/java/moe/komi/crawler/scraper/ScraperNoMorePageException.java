package moe.komi.crawler.scraper;

import moe.komi.crawler.Board;

public class ScraperNoMorePageException extends ScraperException {
    public ScraperNoMorePageException(Board board, String message) {
        super(board, message);
    }
}
