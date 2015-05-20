package moe.komi.crawler.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class JsoupScraperAdapter implements ScraperAdapter {
    @Override
    public Document fetchDoc(String url) throws IOException {
        ArrayList<String> l = new ArrayList();
        Document doc = null;
        int i = 0;
        SocketTimeoutException errorA = null;
        IOException errorB = null;

        while (i++ < 3) {
            try {
                doc = Jsoup
                        .connect(url)
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 4.2.1; zh-tw; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19")
                        .timeout(3000)
                        .get();
                return doc;
            } catch (SocketTimeoutException ex) {
                errorA = ex;
            } catch (IOException e) {
                errorB = e;
            }
        }

        if(errorA != null) {
            throw errorA;
        }

        if(errorB != null) {
            throw errorB;
        }

        return null;
    }
}
