package com.rarchives.ripme.ripper.rippers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.rarchives.ripme.ripper.AbstractHTMLRipper;
import com.rarchives.ripme.utils.Http;

public class AnimationscreencapsRipper extends AbstractHTMLRipper {

    public AnimationscreencapsRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return "animationscreencaps";
    }

    @Override
    public String getDomain() {
        return "animationscreencaps.com";
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://animationscreencaps\\.com/([a-zA-Z0-9_\\-]*)/?$");
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected animationscreencaps URL format: "
                + "animationscreencaps.com/ANIMATION/ - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        return Http.url(url).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Element elem = doc.select("div.wp-pagenavi > span.current + a.page").first();
        if (elem == null) {
            throw new IOException("No more pages");
        }
        String nextPage = elem.attr("href");
        // Some times this returns a empty string
        // This for stops that
        if (nextPage.equals("")) {
            return null;
        } else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Elements selector = doc.select("article > section > div > a > img.thumb");
        //LOGGER.info("Found " + selector.size() + " images");
        for (Element el : selector) {
            String imageSource = el.attr("src");
            if (imageSource.contains("wp.com/")) {
                result.add(imageSource.replace("?w=200&strip=all", ""));
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
