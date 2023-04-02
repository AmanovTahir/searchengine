package searchengine.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.PageModel;

import java.util.List;
import java.util.Optional;

public interface IndexRepository extends JpaRepository<Index, Integer>, JpaSpecificationExecutor<Index> {
    Optional<List<Index>> findAllByPageModel(PageModel pageModel);
    List<Index> findAllByLemma(Lemma lemma);
}
