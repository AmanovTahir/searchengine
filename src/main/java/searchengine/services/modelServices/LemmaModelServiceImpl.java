package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.repository.LemmaRepository;
import searchengine.services.lemmatisator.LemmaFinderService;
import searchengine.services.parser.ParseStateService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Log4j2
@Async
public class LemmaModelServiceImpl implements LemmaModelService {
    private final LemmaFinderService lemmaFinderService;
    private final LemmaRepository lemmaRepository;
    private final IndexModelService indexModelService;
    private final ParseStateService stopService;
    private final ReentrantReadWriteLock lock;

    @Override
    public CompletableFuture<List<Index>> indexPage(PageModel pageModel) {
        if (stopService.isStopped()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        List<Lemma> lemmas = collect(pageModel).join();
        lemmaRepository.saveAll(lemmas);
        List<Index> list = indexModelService.save(lemmas, pageModel).parallelStream().toList();
        lock.writeLock().unlock();
        return CompletableFuture.completedFuture(list);
    }

    @NotNull
    @Async
    protected CompletableFuture<List<Lemma>> collect(PageModel pageModel) {
        Map<String, Integer> lemmasForContent = getLemmasForContent(pageModel);
        lock.writeLock().lock();
        return CompletableFuture.completedFuture(lemmasForContent
                .entrySet()
                .parallelStream()
                .map(map -> lemmaRepository.update(map, pageModel.getSite()))
                .toList());
    }

    private Map<String, Integer> getLemmasForContent(PageModel pageModel) {
        return lemmaFinderService.collect(pageModel.getContent());
    }

//    @Override
//    public CompletableFuture<Lemma> init(PageModel pageModel, Map.Entry<String, Integer> map) {
//        Optional<Lemma> lemmaOptional = lemmaRepository
//                .findFirstByLemmaAndSite(map.getKey(), pageModel.getSite());
//        if (lemmaOptional.isPresent()) {
//            Lemma lemma = lemmaOptional.get();
//            lemma.setFrequency(lemma.getFrequency() + 1);
//            lemma.setRank(map.getValue().floatValue());
//            return CompletableFuture.completedFuture(lemma);
//        } else {
//            return CompletableFuture.completedFuture(Lemma.builder()
//                    .site(pageModel.getSite())
//                    .lemma(map.getKey())
//                    .rank(map.getValue().floatValue())
//                    .frequency(1)
//                    .build());
//        }
//    }

    @NotNull
    @Override
    public CompletableFuture<Set<Lemma>> getLemmasByQuery(Map<Lemma, Double> queryLemmas) {
        double maxPercent = queryLemmas.values().stream().max(Comparator.naturalOrder()).orElse(85D);
        double percent = queryLemmas.size() > 1 ? maxPercent : 100D;
        return CompletableFuture.completedFuture(queryLemmas.entrySet()
                .stream()
                .filter(map -> map.getValue() < percent)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
    }
}
