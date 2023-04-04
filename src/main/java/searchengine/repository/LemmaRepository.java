package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findFirstByLemmaAndSite(String lemma, SiteModel site);

    default Lemma update(Map.Entry<String, Integer> map, SiteModel site) {
        Optional<Lemma> lemmaOptional = findFirstByLemmaAndSite(map.getKey(), site);
        if (lemmaOptional.isPresent()) {
            Lemma lemma = lemmaOptional.get();
            lemma.setFrequency(lemma.getFrequency() + 1);
            lemma.setRank(map.getValue().floatValue());
            return lemma;
        } else {
            return Lemma.builder()
                    .site(site)
                    .lemma(map.getKey())
                    .rank(map.getValue().floatValue())
                    .frequency(1)
                    .build();
        }
    }

    List<Lemma> findAllByLemma(String lemma);
}
