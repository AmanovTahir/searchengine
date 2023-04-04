package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repository.IndexRepository;
import searchengine.services.modelServices.LemmaModelService;
import searchengine.services.modelServices.PageModelService;
import searchengine.services.parser.ParseStateService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Scope("prototype")
@Log4j2
public class PageIndexServiceImpl implements PageIndexService {
    private final IndexRepository indexRepository;
    private final PageModelService pageModelService;
    private final ParseStateService stopService;
    private final LemmaModelService lemmaModelService;


    @SneakyThrows
    @Override
    public CompletableFuture<List<Index>> indexPages(String url, SiteModel siteModel) {
        PageModel pageModel = pageModelService.get(url, siteModel);
        if (isApproved(pageModel)) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        return CompletableFuture.completedFuture(lemmaModelService.indexPage(pageModel).join().parallelStream().toList());
    }

    @Override
    public void indexPage(String url, SiteModel siteModel) {
        PageModel pageModel = pageModelService.update(url, siteModel);
        if (isApproved(pageModel)) {
            return;
        }
        indexRepository.saveAllAndFlush(lemmaModelService.indexPage(pageModel).join().parallelStream().toList());
    }

    private boolean isApproved(PageModel pageModel) {
        return stopService.isStopped() || pageModel.getCode() >= 400;
    }
}
