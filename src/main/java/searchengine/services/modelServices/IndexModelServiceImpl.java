package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.services.parser.ParseStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Component
@Log4j2
public class IndexModelServiceImpl implements IndexModelService {
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final ParseStateService stopService;

    @Override
    public List<Index> save(List<Lemma> lemmas, PageModel pageModel) {
        if (stopService.isStopped()) {
            return new ArrayList<>();
        }
        return collect(lemmas, pageModel).join();
    }

    @Override
    @Async
    public CompletableFuture<List<Index>> collect(List<Lemma> lemmas, PageModel pageModel) {
        return CompletableFuture.completedFuture(lemmas.parallelStream().map(lemma -> init(pageModel, lemma)).toList());
    }

    @Override
    public Index init(PageModel pageModel, Lemma lemma) {
        return Index.builder()
                .pageModel(pageModel)
                .lemma(lemma)
                .id(new Index.IndexKey(pageModel.getId(), lemma.getId()))
                .rank(lemma.getRank())
                .build();
    }

    @Override
    public void delete(PageModel pageModel, SiteModel siteModel) {
        Optional<List<Index>> optionalIndexList = indexRepository.findAllByPageModel(pageModel);
        optionalIndexList.ifPresent(indices -> lemmaRepository.deleteAllInBatch(updateLemmas(indices)));
    }


    private List<Lemma> updateLemmas(List<Index> indices) {
        return indices.stream()
                .map(Index::getLemma)
                .peek(lemma -> lemma.setFrequency(lemma.getFrequency() - 1))
                .filter(lemma -> lemma.getFrequency() == 0)
                .toList();
    }
}
