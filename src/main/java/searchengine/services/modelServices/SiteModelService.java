package searchengine.services.modelServices;

import lombok.SneakyThrows;
import searchengine.config.Site;
import searchengine.model.SiteModel;

import java.util.Optional;

public interface SiteModelService {
    SiteModel save(Site site);

    @SneakyThrows
    SiteModel init(Site site);

    void updateTime(SiteModel siteModel);

    Optional<SiteModel> matchAndGetModel(String url);
}
