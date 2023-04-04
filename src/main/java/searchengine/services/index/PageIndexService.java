package searchengine.services.index;

import searchengine.model.Index;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PageIndexService {

    CompletableFuture<List<Index>> indexPages(String url, SiteModel siteModel);

    void indexPage(String url, SiteModel siteModel);
}
