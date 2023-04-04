package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.LemmaRepository;
import searchengine.services.lemmatisator.LemmaFinder;
import searchengine.services.parser.ParseState;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class LemmaModelServiceImpl implements LemmaModelService {
    private final LemmaFinder lemmaFinder;
    private final LemmaRepository lemmaRepository;
    private final IndexModelService indexModelService;
    private final ParseState parseState;
    private final ReentrantReadWriteLock lock;

    @Override
    public void indexPage(PageModel pageModel) {
        if (parseState.isStopped()) {
            return;
        }
        List<Lemma> lemmas = collect(pageModel);
        indexModelService.save(lemmas, pageModel);
        lock.writeLock().unlock();
    }

    public Lemma init(PageModel pageModel, Map.Entry<String, Integer> map) {
        Optional<Lemma> lemmaOptional = lemmaRepository.findFirstByLemmaAndSite(map.getKey(), pageModel.getSite());
        if (lemmaOptional.isPresent()) {
            Lemma lemma = lemmaOptional.get();
            lemma.setFrequency(lemma.getFrequency() + 1);
            lemma.setRank(map.getValue().floatValue());
            return lemma;
        } else {
            return Lemma.builder()
                    .site(pageModel.getSite())
                    .lemma(map.getKey())
                    .rank(map.getValue().floatValue())
                    .frequency(1)
                    .build();
        }
    }

    @NotNull
    @Override
    public Set<Lemma> getLemmasByQuery(Map<Lemma, Double> queryLemmas) {
        double maxPercent = queryLemmas.values().stream().max(Comparator.naturalOrder()).orElse(85D);
        double percent = queryLemmas.size() > 1 ? maxPercent : 100D;
        return queryLemmas.entrySet()
                .stream()
                .filter(map -> map.getValue() < percent)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public Map<Lemma, Double> getLemmaAllSite(Set<String> lemmas) {
        return lemmas.stream()
                .flatMap(lem -> lemmaRepository.findAllByLemma(lem).stream())
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> (double) lemma.getFrequency() / lemma.getSite().getPageModels().size() * 100));
    }

    @Override
    public Map<Lemma, Double> getLemmaBySite(Set<String> lemmas, SiteModel siteModel) {
        return lemmas.stream()
                .map(string -> lemmaRepository.findFirstByLemmaAndSite(string, siteModel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        lemma -> lemma,
                        lemma -> (double) lemma.getFrequency() / lemma.getSite().getPageModels().size() * 100));
    }

    @Override
    public void deleteLemmas(List<Lemma> lemmas) {
        lemmaRepository.deleteAllInBatch(lemmas);
    }

    @NotNull
    private List<Lemma> collect(PageModel pageModel) {
        Map<String, Integer> lemmasForContent = lemmaFinder.collect(pageModel.getContent());
        lock.writeLock().lock();
        return lemmasForContent
                .entrySet()
                .stream()
                .map(map -> init(pageModel, map))
                .toList();
    }
}
