package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.dto.search.SearchData;
import searchengine.model.PageModel;
import searchengine.services.search.SnippetService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchDataServiceImpl implements SearchDataService {
    private final SnippetService snippetService;

    public SearchData init(String query, Map<PageModel, Double> rRel, PageModel pageModel) {
        return SearchData.builder()
                .title(Jsoup.parse(pageModel.getContent()).title())
                .site(pageModel.getSite().getUrl().replaceFirst("/$", ""))
                .uri(pageModel.getPath())
                .snippet(snippetService.getSnippet(pageModel.getContent(), query))
                .siteName(pageModel.getSite().getName())
                .relevance(rRel.get(pageModel))
                .build();
    }
}
