package com.elbundo.DiscountedWinesapi.repository;

import com.elbundo.DiscountedWinesapi.model.Wine;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WineRepository extends CrudRepository<Wine, Long> {
    List<Wine> findAll();
    List<Wine> findWinesBySite(String site);
    void delete(Wine wine);
    Wine save(Wine wine);
}
