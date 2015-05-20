package moe.komi.crawler;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board implements Serializable {

    public Long id;

    public String name = null;

    public String baseUrl = null;

    public String indexUrl = null;

    public String thumbBaseUrl = null;

    public String srcBaseUrl = null;

    public String mainScript = null;

    public Boolean x = false;

    public Boolean enabled = true;

    public Board() {

    }

    /**
     * Scraper ID
     */
    public Sid sid = null;

    @Override
    public String toString() {
        return this.name;
    }

    public String getPageUrl(String page) {
        return baseUrl + page;
    }

    public String getThreadUrl(String res) {
        return baseUrl + mainScript + "?res=" + res;
    }

    public String getResUrl(String res) {
        return baseUrl + mainScript + "?res=" + res;
    }

    public String getThumbUrl(String name) {
        return thumbBaseUrl + name;
    }

    public String getSrcUrl(String name) {
        return srcBaseUrl + name;
    }

    public Boolean validateSrc(String src) {
        return src.matches(".+?\\.(jpg|gif|swf|png|jpeg)$");
    }

    public String extractSrc(String link) {
        Pattern r = Pattern.compile("/(\\d+\\.(jpg|png|gif|jpeg))$");
        Matcher m = r.matcher(link);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public Boolean validateTh(String th) {
        return th.matches(".+?\\.jpg$");
    }

    public String extractTh(String link) {
        Pattern r = Pattern.compile("/(\\d+s\\.jpg)$");
        Matcher m = r.matcher(link);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public Boolean validateThreadLink(String link) {
        return link.startsWith(mainScript + "?res=");
    }

    public String extractThreadRes(String link) {
        Pattern r = Pattern.compile("res=(\\d+)");
        Matcher m = r.matcher(link);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public Boolean validateTripID(String id) {
        return id.matches("[\\./0-9A-Za-z]+");
    }

}
