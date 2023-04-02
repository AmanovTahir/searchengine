package searchengine.services.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.services.connection.SiteConnectionService;
import searchengine.services.index.PageIndexService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@Scope("prototype")
@RequiredArgsConstructor
@Getter
public class LinkParserServiceImpl implements LinkParserService {
    private static final String REGEX = "[-\\w+/=~_|!:,.;]*[^#?]/?+";
    private static final String[] SUFFIX = new String[]{"jpg", "pdf", "doc", "docx", "mp4"};
    private final PageRepository pageRepository;
    private final Set<String> links;
    private final SiteConnectionService connectionService;
    private final PageIndexService pageIndexServiceImpl;
    private final ParseStateService stopService;

    public Set<String> getLinks(String url, SiteModel site) {
        Set<String> collect = new HashSet<>();
        if (stopService.isStopped()) {
            log.warn("Indexing is stopped " + Thread.currentThread().getName());
            Thread.currentThread().interrupt();
            return collect;
        }
        try {
            Thread.sleep(45);
            collect = collect(site, url);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        return collect;
    }

    private Set<String> collect(SiteModel site, String url) {
        Document doc = connectionService.getHTMLDocument(url);
        return doc.select("a")
                .stream()
                .map(element -> element.attr("abs:href"))
                .distinct()
                .filter(link -> filter(site, link))
                .peek(log::info)
                .peek(link -> pageIndexServiceImpl.indexPages(link, site))
                .collect(Collectors.toSet());
    }

    private boolean filter(SiteModel site, String link) {
        return link.matches(site.getUrl() + REGEX)
                && !stopService.isStopped()
                && links.add(link)
                && Arrays.stream(SUFFIX).noneMatch(link::endsWith);
    }
}
