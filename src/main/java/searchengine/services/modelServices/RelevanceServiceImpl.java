package searchengine.services.modelServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Index;
import searchengine.model.PageModel;
import searchengine.repository.IndexRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RelevanceServiceImpl implements RelevanceService {
    private final IndexRepository indexRepository;
    private final PageModelService pageModelService;

    public List<PageModel> sortDesc(Map<PageModel, Double> rRel) {
        return rRel.entrySet()
                .stream()
                .sorted(Map.Entry.<PageModel, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    public Map<PageModel, Double> getAbsoluteRelevance(Set<PageModel> pageModels) {
        return pageModels.stream()
                .flatMap(page -> indexRepository.findAllByPageModel(page).stream())
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Index::getPageModel, Collectors.summingDouble(Index::getRank)));
    }

    public Map<PageModel, Double> getRelativeRelevance(Set<PageModel> pageModels) {
        Map<PageModel, Double> absoluteRelevance = getAbsoluteRelevance(pageModels);
        return absoluteRelevance.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        map -> map.getValue() / absoluteRelevance.values().stream().max(Double::compareTo).get()));
    }

    public List<PageModel> getRelativePages(Set<PageModel> pageModels) {
        Map<PageModel, Double> relativeRelevance = getRelativeRelevance(pageModels);
        return sortDesc(relativeRelevance);
    }
}
