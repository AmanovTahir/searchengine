package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;


public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findFirstByLemmaAndSite(String lemma, SiteModel site);

    List<Lemma> findAllByLemma(String lemma);

    int countBySite(SiteModel site);
}
