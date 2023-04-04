package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.services.LemmaModelService;
import searchengine.services.PageModelService;
import searchengine.services.parser.ParseState;

@RequiredArgsConstructor
@Component
@Log4j2
public class PageIndex {
    private final PageModelService pageModelService;
    private final ParseState parseState;
    private final LemmaModelService lemmaService;


    public void indexPages(String url, SiteModel siteModel) {
        PageModel page = pageModelService.get(url, siteModel);
        if (isApproved(page)) {
            return;
        }
        lemmaService.indexPage(page);
    }


    public void indexPage(String url, SiteModel siteModel) {
        PageModel page = pageModelService.update(url, siteModel);
        if (isApproved(page)) {
            return;
        }
        lemmaService.indexPage(page);
    }


    private boolean isApproved(PageModel pageModel) {
        return parseState.isStopped() || pageModel.getCode() >= 400;
    }
}
