package searchengine.services;

import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Set;

public interface IndexModelService {

    void save(List<Lemma> lemmas, PageModel pageModel);

    List<Index> collect(List<Lemma> lemmas, PageModel pageModel);

    Index init(PageModel pageModel, Lemma lemma);

    void delete(PageModel pageModel, SiteModel siteModel);

    List<Index> collectIndexesByPages(Set<PageModel> pages);

    Set<PageModel> findPagesByLemma(Lemma lem);
}
