package searchengine.services.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;
import searchengine.services.exception.ErrorMessages;
import searchengine.services.modelServices.SiteModelService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Setter
@Getter
@Log4j2
public class SiteParserServiceImpl extends RecursiveAction implements SiteParserService {
    private final LinkParserService linkService;
    private final SiteRepository siteRepository;
    private final ObjectProvider<SiteParserServiceImpl> provider;
    private final Set<String> links;
    private final ParseStateService state;
    private final SiteModelService siteModelService;
    private String url;
    private SiteModel siteModel;

    @Override
    protected void compute() {
        if (state.isStopped()) {
            log.warn("Indexing is stopped " + Thread.currentThread().getName());
            siteModel.setStatus(Status.FAILED);
            siteModel.setLastError(ErrorMessages.STOPPED_BY_THE_USER.getValue());
            siteModel.setStatusTime(LocalDateTime.now());
            return;
        }
        siteModelService.updateTime(siteModel);
        List<SiteParserServiceImpl> taskList = new ArrayList<>();
        Set<String> links = linkService.getLinks(url, siteModel);
        links.forEach(link -> initService(taskList, link));
        taskList.forEach(ForkJoinTask::join);
    }

    private void initService(List<SiteParserServiceImpl> taskList, String link) {
        provider.forEach(siteParserServiceImpl -> {
            siteParserServiceImpl.setUrl(link);
            siteParserServiceImpl.setSiteModel(siteModel);
            taskList.add(siteParserServiceImpl);
            siteParserServiceImpl.fork();
        });
    }

    @Override
    public SiteModel call() {
        links.clear();
        compute();
        if (siteModel.getStatus() == Status.FAILED) {
            return siteModel;
        }
        siteModel.setStatusTime(LocalDateTime.now());
        siteModel.setStatus(Status.INDEXED);
        return siteModel;
    }
}
