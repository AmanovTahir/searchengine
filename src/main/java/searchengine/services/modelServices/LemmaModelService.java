package searchengine.services.modelServices;

import org.jetbrains.annotations.NotNull;
import searchengine.model.Lemma;
import searchengine.model.PageModel;

import java.util.Map;
import java.util.Set;


public interface LemmaModelService {

    void indexPage(PageModel pageModel);

    Lemma init(PageModel pageModel, Map.Entry<String, Integer> map);

    @NotNull
    Set<Lemma> getLemmasByQuery(Map<Lemma, Double> queryLemmas);
}
