package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;
import searchengine.model.IndexState;
import searchengine.model.SiteModel;
import searchengine.repository.SiteRepository;
import searchengine.services.index.PageIndexService;
import searchengine.services.modelServices.SiteModelService;
import searchengine.services.parser.ParseStateService;
import searchengine.services.parser.SiteParserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
@Log4j2
public class AsyncSiteParser {
    private final SiteRepository siteRepository;
    private final SiteModelService siteModelService;
    private final ObjectProvider<SiteParserServiceImpl> provider;
    private final ParseStateService stateService;
    private final IndexState state;
    private final PageIndexService pageIndexServiceImpl;

    @Async
    public void startIndexingPages(SitesList list) {
        stateService.setState(false);
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<SiteParserServiceImpl> siteParserServices = initService(list);
        try {
            for (Future<SiteModel> siteModelFuture : executorService.invokeAll(siteParserServices)) {
                siteRepository.save(siteModelFuture.get());
            }
            executorService.shutdown();
            state.setIndexing(false);
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private List<SiteParserServiceImpl> initService(SitesList list) {
        List<SiteParserServiceImpl> parsers = new ArrayList<>();
        list.getSites().forEach(site -> provider.forEach(service -> {
            service.setUrl(site.getUrl());
            service.setSiteModel(siteModelService.save(site));
            parsers.add(service);
        }));
        return parsers;
    }

    public void startIndexingPage(String url, SiteModel siteModel) {
        pageIndexServiceImpl.indexPage(url, siteModel);
    }

    @Async
    public void stopIndexing() {
        stateService.setState(true);
    }
}

