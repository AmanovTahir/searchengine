package searchengine.services.search;

import searchengine.dto.search.SearchRequestDto;
import searchengine.dto.search.SearchResponse;


public interface SearchService {
    SearchResponse getResult(SearchRequestDto searchRequestDto);
}
