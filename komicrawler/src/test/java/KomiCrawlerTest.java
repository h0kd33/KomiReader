import org.apache.commons.validator.routines.UrlValidator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import moe.boards.crawler.Board;
import moe.boards.crawler.KomiCrawler;
import moe.boards.crawler.KomiCrawlerException;
import moe.boards.crawler.Post;
import moe.boards.crawler.Sid;
import moe.boards.crawler.Thread;
import moe.boards.crawler.scraper.JsoupScraperAdapter;
import moe.boards.crawler.scraper.Scraper;
import moe.boards.crawler.scraper.ScraperException;

/**
 * Tests for {@link KomiCrawler}.
 *
 * @author touhonoob@gmail.com (Peter Chung)
 */
public class KomiCrawlerTest {
    public static KomiCrawler getCrawler() throws IOException, KomiCrawlerException {
        return new KomiCrawler(new URL("https://cdn.rawgit.com/touhonoob/komica-boards-extractor/5d22de0354d12ff4b0408d7977a39abb8be1d9e8/boards.json"));
    }

    @Test
    public void testGetBoards() throws IOException, KomiCrawlerException {
        /**
         * boards.json
         * http://pastebin.com/NLBbVFaX
         */
        KomiCrawler komiCrawler = getCrawler();
        ArrayList<Board> boards = komiCrawler.getBoards();

        Assert.assertTrue("should have boards", boards.size() > 0);
        for (Board board : boards) {
            Assert.assertTrue("should have sid", board.sid instanceof Sid);
            Assert.assertTrue("sid's value should be string", board.sid.getValue().length() > 0);
            Assert.assertTrue("baseUrl should be valid URL", UrlValidator.getInstance().isValid(board.baseUrl));
            Assert.assertTrue("srcBaseUrl should be valid URL", UrlValidator.getInstance().isValid(board.srcBaseUrl));
            Assert.assertTrue("thumbBaseUrl should be valid URL", UrlValidator.getInstance().isValid(board.thumbBaseUrl));
        }
    }

    @Test(expected = KomiCrawlerException.class)
    public void testGetBoardsError() throws IOException, KomiCrawlerException {
        KomiCrawler komiCrawler = new KomiCrawler(new URL("http://example.com/404"));
    }

    //@Test
    //todo re-enable tests
    public void testScrapers() throws IOException, KomiCrawlerException, ScraperException {
        KomiCrawler komiCrawler = getCrawler();
        ArrayList<Board> boards = komiCrawler.getBoards();

        for (Board board : boards) {
            Scraper scraper = komiCrawler.getScraper(board, new JsoupScraperAdapter());

            ArrayList<Thread> threads = scraper.getPage(null);
            assertThreads(board, threads);

            Integer pageNum = 0;
            do {
                Assert.assertTrue("page " + pageNum + " should has next", scraper.isHasNext());
                threads = scraper.getNextPage();
                assertThreads(board, threads);
            } while (scraper.isHasNext() && ++pageNum < 1);

            Assert.assertTrue("should have more than 2 pages", scraper.isHasNext() && pageNum == 1);

            break;
        }
    }

    private void assertThreads(Board board, ArrayList<Thread> threads) {
        Assert.assertTrue("should have threads", threads.size() > 0);

        for (Thread thread : threads) {
            Assert.assertTrue("should have res", thread.res != null && thread.res.length() > 0);
            //System.out.println("Thread no. " + thread.res);

            Assert.assertTrue("should have posts", thread.posts.size() > 0);
            System.out.println("Thread No." + thread.posts.get(0).no);
            for (Post post : thread.posts) {
                post.print();

                //check img
                if(post.image != null) {
                    Assert.assertTrue("src should be valid: " + post.image.src, board.validateSrc(post.image.src));
                    Assert.assertTrue("th should be valid: " + post.image.th, board.validateTh(post.image.th));
                }

                //check content
                Assert.assertTrue("should have content", post.com.length() > 0);

                //check tripid
                Assert.assertTrue("should have tripid: " + post.tripId, board.validateTripID(post.tripId));

                //check no
                Assert.assertTrue("should have no", post.no.matches("\\d+"));

                //check date
                Assert.assertTrue("should have date", post.date != null);
            }
        }
    }

}