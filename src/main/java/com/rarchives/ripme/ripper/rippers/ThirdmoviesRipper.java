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

public class ThirdmoviesRipper extends AbstractHTMLRipper {

    private static final String DOMAIN = "thirdmovies.com";
    private static final String HOST = "thirdmovies";

    public ThirdmoviesRipper(URL url) throws IOException {
        super(url);
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
    }

    @Override
    public String getGID(URL url) throws MalformedURLException {
        Pattern p = Pattern.compile("https?://www\\.thirdmovies\\.com/screencaps/([a-zA-Z0-9_\\-]*)/?$");
        
        Matcher m = p.matcher(url.toExternalForm());
        if (m.matches()) {
            return m.group(1);
        }
        throw new MalformedURLException("Expected thirdmovies URL format: "
                + "www.thirdmovies.com/screencaps/MOVIENAME/ - got " + url + " instead");
    }

    @Override
    public Document getFirstPage() throws IOException {
        // "url" is an instance field of the superclass
        // set page size to 300
        URL newUrl = new URL(url + "?page=1&limit=100");
        return Http.url(newUrl).get();
    }

    @Override
    public Document getNextPage(Document doc) throws IOException {
        // Find next page
        Element elem = doc.select("div > ul > li.next_page > a").first();
        
        if (elem == null) {
            throw new IOException("No more pages");
        }

        String nextPage = elem.attr("abs:href");
        // Some times this returns a empty string
        // Next page is not deactivated, check if we are actually on the last page
        if (nextPage.equals("") || doc.location().contains(elem.attr("href"))) {
            throw new IOException("No more pages");
        } else {
            return Http.url(nextPage).get();
        }
    }

    @Override
    public List<String> getURLsFromPage(Document doc) {
        List<String> result = new ArrayList<>();
        Elements selector = doc.select("#section-gallery > div.picture-thumb-block > div.gallerythumb > a");
        //LOGGER.info("Found " + selector.size() + " images");
        for (Element el : selector) {
            String imageSource = el.attr("href");
            if (imageSource.contains("cdn.thirdmovies.com/")) {
                result.add(imageSource.split("\\?")[0]);
            }
        }
        return result;
    }

    @Override
    public void downloadURL(URL url, int index) {
        addURLToDownload(url, getPrefix(index));
    }
}
