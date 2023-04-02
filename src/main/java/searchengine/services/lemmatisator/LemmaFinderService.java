package searchengine.services.lemmatisator;


import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Lemma;

import java.util.Map;
import java.util.Set;

public interface LemmaFinderService {
    Map<String, Integer> collect(String text);

    Map<String, String> collectLemmasAndQueryWord(String text);

    Set<String> collectLemmasToSet(String text);

    void getLemmaInfoSet(String text);

    public Map<Lemma, Double> getSearchQueryLemma(SearchRequestDto searchRequestDto);
}
