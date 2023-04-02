package searchengine.services.index;

import searchengine.model.SiteModel;

public interface PageIndexService {

    void indexPages(String url, SiteModel siteModel);

    void indexPage(String url, SiteModel siteModel);
}
