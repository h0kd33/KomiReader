package moe.komi.crawler.scraper;

import org.jsoup.nodes.Document;

public interface ScraperAdapter {
    public Document fetchDoc(String url) throws Throwable;
}
