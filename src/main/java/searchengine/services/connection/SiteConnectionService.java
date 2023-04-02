package searchengine.services.connection;

import org.jsoup.nodes.Document;

public interface SiteConnectionService {
    Document getHTMLDocument(String url);

}
