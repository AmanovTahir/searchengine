package searchengine.services.modelServices;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IndexModelService {

    List<Index> save(List<Lemma> lemmas, PageModel pageModel);

    CompletableFuture<List<Index>> collect(List<Lemma> lemmas, PageModel pageModel);

    Index init(PageModel pageModel, Lemma lemma);

    void delete(PageModel pageModel, SiteModel siteModel);
}
