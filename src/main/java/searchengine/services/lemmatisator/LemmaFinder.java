package searchengine.services.lemmatisator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;
import searchengine.services.LemmaModelService;
import searchengine.services.SiteModelService;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LemmaFinder {
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС", "МС-П", "ВВОДН"};
    private static final String EXCESS_TAGS = "<br>|<p>|&[a-z]+;";
    private final LuceneMorphology luceneMorphology;
    private final LemmaModelService lemmaModelService;
    private final SiteModelService siteModelService;

    public LemmaFinder(LuceneMorphology luceneMorphology,
                       @Lazy LemmaModelService lemmaModelService,
                       SiteModelService siteModelService) {
        this.luceneMorphology = luceneMorphology;
        this.lemmaModelService = lemmaModelService;
        this.siteModelService = siteModelService;
    }

    public Map<String, Integer> collect(String text) {
        Map<String, Integer> lemmas = new ConcurrentHashMap<>();
        Arrays.stream(arrayContainsRussianWords(htmlToText(text)))
                .filter(word -> !word.isEmpty())
                .filter(this::hasParticleProperty)
                .filter(word -> !luceneMorphology.getNormalForms(word).isEmpty())
                .filter(word -> word.length() > 1)
                .map(word -> luceneMorphology.getNormalForms(word).get(0))
                .forEach(word -> lemmas.merge(word, 1, Integer::sum));
        return lemmas;
    }

    public Map<String, String> collectLemmasAndQueryWord(String text) {
        Map<String, String> lemmas = new ConcurrentHashMap<>();
        Arrays.stream(arrayContainsRussianWords(htmlToText(text)))
                .filter(this::hasParticleProperty)
                .filter(word -> !luceneMorphology.getNormalForms(word).isEmpty())
                .filter(word -> word.length() > 1)
                .forEach(word -> {
                    if (!luceneMorphology.getNormalForms(word).isEmpty()) {
                        lemmas.put(luceneMorphology.getNormalForms(word).get(0), word);
                    }
                });
        return lemmas;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private boolean hasParticleProperty(String value) {
        return luceneMorphology
                .getMorphInfo(value)
                .stream().noneMatch(word -> Arrays.stream(PARTICLES_NAMES)
                        .anyMatch(part -> word.toUpperCase().contains(part)));
    }

    public Map<Lemma, Double> getSearchQueryLemma(SearchRequestDto searchRequestDto) {
        String query = searchRequestDto.getQuery();
        String site = searchRequestDto.getSite();
        SiteModel siteModel = siteModelService.findSiteByUrl(site);
        if (siteModel != null) {
            return lemmaModelService.getLemmaBySite(collect(query).keySet(), siteModel);
        }
        return lemmaModelService.getLemmaAllSite(collect(query).keySet());
    }

    private String htmlToText(String html) {
        String body = html.replaceAll(EXCESS_TAGS, " ");
        return Jsoup.clean(
                body, "", Safelist.none(),
                new Document.OutputSettings().prettyPrint(true)
        ).trim();
    }
}
