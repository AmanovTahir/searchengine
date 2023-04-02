package searchengine.services.lemmatisator;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;
import searchengine.repository.LemmaRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LemmaFinderServiceImpl implements LemmaFinderService {
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС", "МС-П", "ВВОДН"};
    private static final String EXCESS_TAGS = "<br>|<p>|&[a-z]+;";
    private final LuceneMorphology luceneMorphology;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    @Override
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

    @Override
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


    @Override
    public Set<String> collectLemmasToSet(String text) {
        return Arrays.stream(arrayContainsRussianWords(text))
                .filter(word -> !word.isEmpty())
                .filter(word -> word.length() > 1)
                .filter(this::isCorrectWordForm)
                .filter(this::hasParticleProperty)
                .map(word -> luceneMorphology.getNormalForms(word).get(0))
                .collect(Collectors.toSet());
    }

    @Override
    public void getLemmaInfoSet(String text) {
        Arrays.stream(arrayContainsRussianWords(text))
                .map(luceneMorphology::getMorphInfo)
                .forEach(System.out::println);
    }


    private boolean hasParticleProperty(String value) {
        return luceneMorphology
                .getMorphInfo(value)
                .stream().noneMatch(word -> Arrays.stream(PARTICLES_NAMES)
                        .anyMatch(part -> word.toUpperCase().contains(part)));
    }


    private boolean isCorrectWordForm(String word) {
        return luceneMorphology.getNormalForms(word)
                .stream()
                .anyMatch(value -> !value.matches(WORD_TYPE_REGEX));
    }

    @Override
    public Map<Lemma, Double> getSearchQueryLemma(SearchRequestDto searchRequestDto) {
        String query = searchRequestDto.getQuery();
        String site = searchRequestDto.getSite();
        Optional<SiteModel> modelOptional = siteRepository.findFirstByUrlIgnoreCase(site);
        return modelOptional.map(siteModel
                        -> getLemmaBySite(query, siteModel))
                .orElseGet(()
                        -> getLemmaAllSite(query));
    }

    @NotNull
    private Map<Lemma, Double> getLemmaAllSite(String query) {
        return collect(query).keySet().stream()
                .flatMap(lem -> lemmaRepository.findAllByLemma(lem).stream())
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> (double) lemma.getFrequency() / lemma.getSite().getPageModels().size() * 100));
    }

    @NotNull
    private Map<Lemma, Double> getLemmaBySite(String query, SiteModel siteModel) {
        return collect(query).keySet().stream()
                .map(string -> lemmaRepository.findFirstByLemmaAndSite(string, siteModel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> (double) lemma.getFrequency() / lemma.getSite().getPageModels().size() * 100));
    }


    private String htmlToText(String html) {
        String body = html.replaceAll(EXCESS_TAGS, " ");
        return Jsoup.clean(
                body, "", Safelist.none(),
                new Document.OutputSettings().prettyPrint(true)
        ).trim();
    }
}
