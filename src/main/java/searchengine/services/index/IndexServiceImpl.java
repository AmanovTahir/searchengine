package searchengine.services.index;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.model.IndexState;
import searchengine.model.SiteModel;
import searchengine.services.AsyncSiteParser;
import searchengine.services.exception.ApiRequestException;
import searchengine.services.exception.ErrorMessages;
import searchengine.services.exception.FaultResponse;
import searchengine.services.modelServices.SiteModelService;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final IndexState indexState;
    private final SitesList sitesList;
    private final AsyncSiteParser parser;
    private final SiteModelService siteModelService;

    @Override
    public IndexResponse startIndexing() {
        if (indexState.isIndexing()) {
            throwException(ErrorMessages.INDEXING_ALREADY_STARTED);
        }
        parser.startIndexingPages(sitesList);
        indexState.setIndexing(true);
        return new IndexResponse(true);
    }

    @Override
    public IndexResponse stopIndexing() {
        if (!indexState.isIndexing()) {
            throwException(ErrorMessages.INDEXING_NOT_STARTED);
        }
        parser.stopIndexing();
        indexState.setIndexing(false);
        return new IndexResponse(true);
    }


    @Override
    public IndexResponse indexPage(String url) {
        SiteModel siteModel = siteModelService.matchAndGetModel(url)
                .orElseThrow(() -> new ApiRequestException(HttpStatus.NOT_FOUND,
                        new FaultResponse(false, ErrorMessages.PAGE_NOT_FOUND.getValue())));

        parser.startIndexingPage(url, siteModel);
        indexState.setIndexing(false);
        return new IndexResponse(true);

    }

    private void throwException(ErrorMessages errorMessages) {
        throw new ApiRequestException(HttpStatus.FORBIDDEN, new FaultResponse(false, errorMessages.getValue()));
    }
}
