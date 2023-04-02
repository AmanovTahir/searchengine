package searchengine.services.parser;

import searchengine.model.SiteModel;

import java.util.Set;

public interface LinkParserService {

    Set<String> getLinks(String url, SiteModel site);

}
