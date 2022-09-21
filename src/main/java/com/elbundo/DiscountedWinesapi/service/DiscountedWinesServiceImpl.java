package com.elbundo.DiscountedWinesapi.service;

import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import com.elbundo.DiscountedWinesapi.repository.WineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class DiscountedWinesServiceImpl implements DiscountedWinesService {
    private final List<AbstractParser> handlers = new ArrayList<>();
    private final WineRepository repository;

    public DiscountedWinesServiceImpl(WineRepository repository, AbstractParser... list) {
        this.repository = repository;
        handlers.addAll(Arrays.asList(list));
    }
    @Override
    public List<Wine> discountedWines() {
        List<Wine> result = new ArrayList<>();
        for(AbstractParser handler : handlers) {
            List<Wine> allDiscountWines = handler.getAllWines();
            if(allDiscountWines.isEmpty())
                continue;
            List<Wine> storedWines = deleteOldWines(repository.findWinesBySite(handler.getSiteUrl()));
            allDiscountWines.removeIf(storedWines::contains);
            log.info("New wines: {}", allDiscountWines);
            allDiscountWines.forEach(repository::save);
            result.addAll(allDiscountWines);
        }
        Collections.shuffle(result);
        return result;
    }

    private List<Wine> deleteOldWines(List<Wine> storedWines) {
        List<Wine> oldWine = new ArrayList<>();
        for(Wine wine : storedWines) {
            if((new Date().getTime() - wine.getLastModified().getTime()) / (24 * 60 * 60 * 1000) >= 7) {
                oldWine.add(wine);
                repository.delete(wine);
                storedWines.remove(wine);
            }
        }
        log.info("Discount has ended for : {}", oldWine);
        return storedWines;
    }
}
