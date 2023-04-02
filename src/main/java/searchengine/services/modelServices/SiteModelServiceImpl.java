package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteModel;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;
import searchengine.services.connection.SiteConnectionService;
import searchengine.services.exception.ApiRequestException;
import searchengine.services.exception.ErrorMessages;
import searchengine.services.exception.FaultResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiteModelServiceImpl implements SiteModelService {
    private final SiteRepository siteRepository;
    private final SiteConnectionService connectionService;
    private final SitesList sitesList;

    @Override
    public SiteModel save(Site site) {
        SiteModel siteModel = init(site);
        siteRepository.findFirstByUrlIgnoreCase(siteModel.getUrl()).ifPresent(siteRepository::delete);
        siteRepository.save(siteModel);
        return siteModel;
    }

    @SneakyThrows
    @Override
    public SiteModel init(Site site) {
        return SiteModel.builder()
                .status(getResponseCode(site) >= 400 ? Status.FAILED : Status.INDEXING)
                .statusTime(LocalDateTime.now())
                .lastError(getResponseCode(site) >= 400 ? ErrorMessages.SITE_IS_UNAVAILABLE.getValue() : null)
                .url(site.getUrl())
                .name(site.getName())
                .pageModels(new HashSet<>())
                .lemmas(new HashSet<>())
                .build();
    }

    @Override
    @Async
    public void updateTime(SiteModel siteModel) {
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    @Override
    public Optional<SiteModel> matchAndGetModel(String url) {
        if (getSite(url).isEmpty()) {
            return Optional.empty();
        }
        Optional<SiteModel> siteModel = getSite(url)
                .flatMap(site -> siteRepository.findFirstByUrlIgnoreCase(site.getUrl()));
        if (siteModel.isEmpty()) {
            return Optional.of(siteRepository.save(init(getSite(url).get())));
        }
        return siteModel;
    }

    private Optional<Site> getSite(String url) {
        return Optional.ofNullable(sitesList.getSites().stream()
                .filter(site -> getHost(site.getUrl()).equals(getHost(url)))
                .findAny()
                .orElseThrow(() -> new ApiRequestException(HttpStatus.NOT_FOUND,
                        new FaultResponse(false, ErrorMessages.PAGE_NOT_FOUND.getValue()))));
    }

    private String getHost(String url) {
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new ApiRequestException(HttpStatus.NOT_FOUND,
                    new FaultResponse(false, ErrorMessages.PAGE_NOT_FOUND.getValue()));
        }
    }

    private int getResponseCode(@NotNull Site site) {
        return connectionService.getHTMLDocument(site.getUrl()).connection().response().statusCode();
    }
}
