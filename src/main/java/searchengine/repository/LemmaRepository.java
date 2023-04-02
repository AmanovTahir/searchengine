package searchengine.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Optional;


public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Optional<Lemma> findFirstByLemmaAndSite(String lemma, SiteModel site);

    List<Lemma> findAllByLemma(String lemma);
}
