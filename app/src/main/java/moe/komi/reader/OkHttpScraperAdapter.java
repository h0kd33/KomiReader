package moe.komi.reader;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import moe.komi.crawler.scraper.ScraperAdapter;

import java.io.IOException;
import java.io.Serializable;

public class OkHttpScraperAdapter implements ScraperAdapter, Serializable {

    public static final String TAG = "OkHttpScraper";

    private static transient OkHttpClient client;

    @Override
    public Document fetchDoc(String url) throws Throwable {
        if(client == null) {
            client = new OkHttpClient();
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", System.getProperty("http.agent"))
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            Exception e = new IOException("OkHttp Error Unexpected code " + response);
            Log.d(TAG, e.getStackTrace().toString());
            Crashlytics.logException(e);
            throw e;
        }

        return Jsoup.parse(response.body().string());
    }
}
