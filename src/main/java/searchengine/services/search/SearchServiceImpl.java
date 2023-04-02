package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResponse;
import searchengine.model.PageModel;
import searchengine.services.exception.ApiRequestException;
import searchengine.services.exception.ErrorMessages;
import searchengine.services.exception.FaultResponse;
import searchengine.services.modelServices.PageModelService;
import searchengine.services.modelServices.RelevanceService;
import searchengine.services.modelServices.SearchDataService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class SearchServiceImpl implements SearchService {
    private final PageModelService pageModelService;
    private final SearchDataService dataService;
    private final RelevanceService relevanceService;

    @Override
    public SearchResponse getResult(SearchRequestDto searchRequestDto) {
        String query = searchRequestDto.getQuery();
        if (query.isEmpty() || query.trim().isBlank()) {
            throwException(HttpStatus.BAD_REQUEST, ErrorMessages.EMPTY_SEARCH);
        }
        return initSearches(searchRequestDto);
    }

    @NotNull
    private SearchResponse initSearches(SearchRequestDto searchRequestDto) {
        Set<PageModel> pageModels = pageModelService.getSearchQueryPages(searchRequestDto);
        Map<PageModel, Double> relevance = relevanceService.getRelativeRelevance(pageModels);
        List<PageModel> sortedPageModels = relevanceService.sortDesc(relevance);
        List<PageModel> pageable = pageModelService.getPageable(searchRequestDto, sortedPageModels);
        List<SearchData> dataList = initDataList(searchRequestDto.getQuery(), relevance, pageable);
        return new SearchResponse(true, sortedPageModels.size(), dataList);
    }

    private boolean checkList(List<SearchData> dataList) {
        boolean empty = dataList.isEmpty();
        if (empty) {
            throwException(HttpStatus.NOT_FOUND, ErrorMessages.NOT_FOUND);
        }
        return empty;
    }

    @NotNull
    private List<SearchData> initDataList(String query, Map<PageModel, Double> rRel, List<PageModel> pageModels) {
        return pageModels
                .stream()
                .map(page -> dataService.init(query, rRel, page))
                .toList();
    }

    private void throwException(HttpStatus status, ErrorMessages errorMessages) {
        throw new ApiRequestException(status, new FaultResponse(false, errorMessages.getValue()));
    }
}