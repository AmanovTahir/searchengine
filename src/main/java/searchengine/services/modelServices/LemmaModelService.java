package searchengine.services.modelServices;

import org.jetbrains.annotations.NotNull;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public interface LemmaModelService {

    CompletableFuture<List<Index>> indexPage(PageModel pageModel);

//    CompletableFuture<Lemma> init(PageModel pageModel, Map.Entry<String, Integer> map);

    @NotNull
    CompletableFuture<Set<Lemma>> getLemmasByQuery(Map<Lemma, Double> queryLemmas);
}
