package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.services.modelServices.LemmaModelService;
import searchengine.services.modelServices.PageModelService;
import searchengine.services.parser.ParseStateService;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@RequiredArgsConstructor
@Service
@Log4j2
public class PageIndexServiceImpl implements PageIndexService {
    private final PageModelService pageModelService;
    private final ParseStateService stopService;
    private final LemmaModelService lemmaService;
    private final ReentrantReadWriteLock lock;


    @Override
    public void indexPages(String url, SiteModel siteModel) {
        PageModel pageModel = pageModelService.get(url, siteModel);
        if (isApproved(pageModel)) {
            return;
        }
        lemmaService.indexPage(pageModel);
    }

    @Override
    public void indexPage(String url, SiteModel siteModel) {
        PageModel pageModel = pageModelService.update(url, siteModel);
        if (isApproved(pageModel)) {
            return;
        }
        lemmaService.indexPage(pageModel);
    }

    private boolean isApproved(PageModel pageModel) {
        return stopService.isStopped() || pageModel.getCode() >= 400;
    }
}
