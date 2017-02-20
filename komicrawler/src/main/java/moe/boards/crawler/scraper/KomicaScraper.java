package moe.boards.crawler.scraper;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import moe.boards.crawler.Board;
import moe.boards.crawler.Image;
import moe.boards.crawler.Post;
import moe.boards.crawler.Sid;
import moe.boards.crawler.Thread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author touhonoob@gmail.com (Peter Chung)
 */
public class KomicaScraper extends Scraper {
    public static final Sid sid = Sid.KOMICA;

    public KomicaScraper(Board board, ScraperAdapter scraperAdapter) {
        super(board, scraperAdapter);
    }

    @Override
    public ArrayList<Thread> getPage(String pageNum) throws ScraperException {
        System.out.println("KomicaScraper::getPage(" + pageNum + ")");

        String pageUrl = pageNum==null ? board.indexUrl : board.getPageUrl(pageNum);
        Document doc;

        try {
            doc = scraperAdapter.fetchDoc(pageUrl);
        } catch (Throwable throwable) {
            throw new ScraperException(this.board, "KomicaScraper can't connect to " + pageUrl);
        }

        nextPageLink = getNextPageLink(doc);
        hasNext = nextPageLink == null ? false : true;

        System.out.println("KomicaScraper::getNextPageLink=" + nextPageLink);

        return currentPageThreads = getThreads(doc);
    }

    private String getNextPageLink(Document doc) throws ScraperException {
        //Get Pagination Table
        Element table = doc.select("table[align=left]").first();
        if (table == null) {
            throw new ScraperException(this.board, "pagination table selection failed");
        }

        //Get current page indicator
        Element b = table.select("b").first();
        if (b == null) {
            throw new ScraperException(this.board, "current page indicator selection failed");
        }

        //Get next page link
        Element a = b.nextElementSibling();
        if (a == null || a.nodeName() != "a" || a.attr("href").trim().length() == 0) {
            //Doesn't have next page
            return null;
        } else {
            //Found next page
            return a.attr("href");
        }
    }

    private ArrayList<Thread> getThreads(Document doc) throws ScraperException {
        System.out.println("KomicaScraper::getThreads");

        ArrayList<Thread> threads = new ArrayList<>();

        //Get threads form
        Elements forms = doc.select("form");
        if (forms.size() < 2) {
            throw new ScraperException(this.board, "threads form selection failed");
        }
        Element form = forms.get(1);

        //SAX Parser
        List<Node> nodes = form.childNodes();
        Thread currentThread = new Thread();
        Post currentPost = new Post();
        for (Node node : nodes) {
            //System.out.println(node.nodeName());

            if (node instanceof Element) {
                switch (node.nodeName()) {
                    case "input":
                        //extract post no
                        if (node.attr("value") == "delete") {
                            currentPost.no = node.attr("name");
                        }
                        break;

                    case "table":
                        if(node.hasAttr("xwidth")) {
                            //New reply
                            //System.out.println("table: new reply post");
                            currentPost = new Post();

                            //Extract id string
                            Element td = ((Element) node).select("td[bgcolor=#F0E0D6]").first();
                            if (td == null) {
                                throw new ScraperException(this.board, "can't extract content td from reply table");
                            }
                            for (Node textNode : td.textNodes()) {
                                currentPost = extractIDString(currentPost, (TextNode) textNode);
                            }
                            checkExtractIDString(currentPost);

                            //Extract content
                            Element blockquote = td.select("blockquote").first();
                            if (blockquote == null) {
                                throw new ScraperException(this.board, "can't extract blockquote from reply table");
                            }
                            currentPost.setCom(blockquote.html());

                            //Extract image
                            Elements anchors = td.select("a");
                            for(Element a : anchors) {
                                Element img = a.select("img").first();
                                if(img != null) {
                                    currentPost.image = getImage(a.attr("href"), img.attr("src"));
                                    break;
                                }
                            }

                            currentThread.addPost(currentPost);
                        }
                        break;

                    case "blockquote":
                        //Set post content
                        currentPost.setCom(((Element) node).html());

                        //firstpost
                        //System.out.println("blockquote: firstpost");
                        currentThread.addPost(currentPost);
                        break;

                    case "a":
                        String link = node.attr("href");

                        //Check & Extract thread link
                        //Ex: "回應有5篇被省略。要閱讀所有回應請按下返信連結。"
                        if (board.validateThreadLink(link)) {
                            String res = board.extractThreadRes(link);
                            if (res == null)
                                throw new ScraperException(this.board, "can't extract res from thread link: " + link);
                            currentThread.res = res;
                            break;
                        }

                        //Check & Extract image
                        Element img = ((Element) node).select("img").first();
                        if (img instanceof Element && board.validateTh(img.attr("src")) && board.validateSrc(node.attr("href"))) {
                            String th = img.attr("src");
                            String src = node.attr("href");
                            currentPost.image = getImage(src, th);
                            break;
                        }

                        break;

                    case "font":
                        //extract omitted reply count
                        if(node.attr("color").endsWith("707070")) {
                            Pattern r = Pattern.compile(".*?(\\d+).*");
                            Matcher m = r.matcher(((Element) node).text());
                            if(m.find()) {
                                currentThread.ignoredNumber = Integer.parseInt(m.group(1));
                            }
                        }
                        break;

                    case "hr":
                        //System.out.println("hr: new thread");

                        //check last first post
                        checkExtractIDString(currentThread.posts.get(0));

                        threads.add(currentThread);
                        currentThread = new Thread();
                        currentPost = new Post();
                        break;
                }
            } else if (node instanceof TextNode) {
                //extract ID string for firstpost
                currentPost = extractIDString(currentPost, (TextNode) node);
            }
        }

        return threads;
    }

    /**
     * Extract Date + ID + No
     * Ex: " 15/02/14(六)07:14:32 ID:F.OqpZFA No.6135732"
     * @return Post
     */
    private Post extractIDString(Post post, TextNode node) {
        Pattern r = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{2}).+?(\\d{2}):(\\d{2}):(\\d{2}) ID:([\\./0-9A-Za-z]+?) No\\.(\\d+)");
        Matcher m = r.matcher(node.text());
        if (m.find()) {
            Integer Y = Integer.parseInt(m.group(1)) + 2000, //year
                    M = Integer.parseInt(m.group(2)) - 1, //month
                    D = Integer.parseInt(m.group(3)), //day
                    H = Integer.parseInt(m.group(4)), //hours
                    I = Integer.parseInt(m.group(5)), //minutes
                    S = Integer.parseInt(m.group(6)); //seconds
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
            cal.set(Y, M, D, H, I, S);
            post.date = cal;

            post.tripId = m.group(7);
            post.no = m.group(8);
        }

        return post;
    }

    private void checkExtractIDString(Post post) throws ScraperException {
        if(post.no == null) {
            throw new ScraperException(this.board, "can't extract No. from reply table");
        }

        if(post.tripId == null) {
            throw new ScraperException(this.board, "can't extract tripId from reply table");
        }

        if(post.date == null) {
            throw new ScraperException(this.board, "can't extract date from reply table");
        }
    }

    private Image getImage(String src, String th) {
        return new Image(src.replace("-cf", ""), th.replace("-cf", ""));
        //return new Image(src, th);
    }

    @Override
    public Thread getFullThread(String res) throws ScraperException {
        System.out.println("KomicaScraper::getThread(" + res + ")");

        String threadUrl = board.getThreadUrl(res);
        Document doc;

        try {
            doc = scraperAdapter.fetchDoc(threadUrl);
        } catch (Throwable throwable) {
            throw new ScraperException(this.board, "KomicaScraper can't connect to " + threadUrl);
        }

        ArrayList<Thread> threads = getThreads(doc);
        if(threads.size() > 0) {
            return threads.get(0);
        } else {
            throw new ScraperException(this.board, "KomicaScraper can't extract thread from " + threadUrl);
        }
    }


}
