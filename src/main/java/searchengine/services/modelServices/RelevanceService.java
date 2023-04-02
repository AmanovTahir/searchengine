package searchengine.services.modelServices;

import searchengine.model.PageModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RelevanceService {

    List<PageModel> sortDesc(Map<PageModel, Double> rRel);

    Map<PageModel, Double> getAbsoluteRelevance(Set<PageModel> pageModels);

    Map<PageModel, Double> getRelativeRelevance(Set<PageModel> pageModels);

    List<PageModel> getRelativePages(Set<PageModel> pageModels);

}
