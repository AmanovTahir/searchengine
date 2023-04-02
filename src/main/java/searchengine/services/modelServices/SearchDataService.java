package searchengine.services.modelServices;

import searchengine.dto.search.SearchData;
import searchengine.model.PageModel;

import java.util.Map;

public interface SearchDataService {
    public SearchData init(String query, Map<PageModel, Double> rRel, PageModel pageModel);
}
