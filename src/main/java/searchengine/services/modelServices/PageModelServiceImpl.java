package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchRequestDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.PageRepository;
import searchengine.services.connection.SiteConnectionService;
import searchengine.services.lemmatisator.LemmaFinderService;
import searchengine.services.parser.ParseStateService;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PageModelServiceImpl implements PageModelService {
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final ParseStateService state;
    private final SiteConnectionService siteConnectionService;
    private final IndexModelService indexModelService;
    private final LemmaFinderService lemmaFinderService;
    private final LemmaModelService lemmaModelService;

    @Override
    public PageModel get(String url, SiteModel model) {
        PageModel pageModel = init(url, model);
        if (state.isStopped()) {
            return pageModel;
        }
        return pageRepository.save(pageModel);
    }

    @Override
    public PageModel update(String url, SiteModel siteModel) {
        PageModel pageModel = init(url, siteModel);
        Optional<PageModel> pageOptional = pageRepository.findFirstByPathAndSite(pageModel.getPath(), siteModel);
        pageOptional.ifPresent(model -> {
            pageRepository.deleteById(model.getId());
            indexModelService.delete(model, siteModel);
        });
        return pageRepository.save(pageModel);
    }

    @Override
    public PageModel init(String url, SiteModel site) {
        Document htmlDocument = siteConnectionService.getHTMLDocument(url);
        return PageModel.builder()
                .url(url)
                .path(URI.create(htmlDocument.location()).getPath())
                .site(site)
                .code(htmlDocument.connection().response().statusCode())
                .content(htmlDocument.html())
                .build();
    }

    @Override
    public Set<PageModel> getSearchQueryPages(SearchRequestDto searchRequestDto) {
        Map<Lemma, Double> queryLemmas = lemmaFinderService.getSearchQueryLemma(searchRequestDto);
        Set<Lemma> lemmas = lemmaModelService.getLemmasByQuery(queryLemmas).join();
        Optional<Lemma> lemmaOptional = lemmas.stream().findFirst();
        return lemmaOptional.map(lemma -> getPagesBySearch(lemmas, lemma)).orElse(Collections.emptySet());
    }

    @NotNull
    private Set<PageModel> getPagesBySearch(Set<Lemma> lemmas, Lemma lem) {
        Set<PageModel> result = indexRepository.findAllByLemma(lem)
                .stream()
                .map(Index::getPageModel)
                .collect(Collectors.toSet());
        lemmas.stream()
                .map(lemma -> indexRepository.findAllByLemma(lemma)
                        .stream()
                        .map(Index::getPageModel)
                        .collect(Collectors.toSet()))
                .forEach(result::retainAll);
        return result;
    }

    @Override
    public List<PageModel> getPageable(SearchRequestDto searchRequestDto, List<PageModel> result) {
        return result.stream().skip(searchRequestDto.getOffset()).limit(searchRequestDto.getLimit()).toList();
    }
}
