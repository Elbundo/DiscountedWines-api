package com.elbundo.DiscountedWinesapi.controller;

import com.elbundo.DiscountedWinesapi.handlers.*;
import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import com.elbundo.DiscountedWinesapi.repository.WineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/wines")
public class DiscountedWinesController {
    private final List<AbstractParser> handlers = new ArrayList<>();
    private final WineRepository repository;

    public DiscountedWinesController(WineRepository repository, AbstractParser... list) {
        this.repository = repository;
        handlers.addAll(Arrays.asList(list));
    }

    //TODO перенести всю логику в сервис

    @GetMapping("/discounted")
    public List<Wine> discountedWines() {
        //TODO реализовать один из вариантов (предпочтительнее второй)
        //1. Перебираем таблицу кандидатов на исключение и удаляем из основный таблицы
        //      все вина которые находятся в кандидатах больше n дней
        //2. Храним в базе дату последнего появления вина на сайте, каждый раз обновляем таблицу,
        //      вина на которые скидка закончилась удаляются только если прошло больше n дней
        List<Wine> result = new ArrayList<>();
        for(AbstractParser handler : handlers){
            List<Wine> allDiscountWines = handler.getAllWines();
            log.info(handler.getSiteUrl() + " " + allDiscountWines + "; " + allDiscountWines.size());
            if(allDiscountWines.isEmpty())
                continue;
            //Удалить из списка проверенных вина, скидка на которые закончилась
            List<Wine> storedWines = repository.findWinesBySite(handler.getSiteUrl());
            List<Wine> storedWinesCopy = new ArrayList<>(storedWines);
            storedWines.removeIf(allDiscountWines::contains);
            //не удалять, а доюавить таблицу кандидатов на исключение
            storedWines.forEach(repository::delete);
            log.info("Wines for which the discount has ended: \n" + storedWines);
            //Удаляем из списка скидочных вин все вина, которые есть в списке checked для этого сайта
            allDiscountWines.removeIf(storedWinesCopy::contains);
            //Добавляем все скидочные вина в список проверенных для этого сайта
            allDiscountWines.forEach(repository::save);
            log.info("New wines: \n" + allDiscountWines);
            result.addAll(allDiscountWines);
        }
        Collections.shuffle(result);
        log.info("In total " + result.size() + " wines");
        return result;
    }

    @GetMapping("/winelab")
    public List<Wine> wineLabDiscountedWines() {
        return new WineLab().getAllWines();
    }
    @GetMapping("/simplewine")
    public List<Wine> simpleWineDiscountedWines() {
        return new SimpleWine().getAllWines();
    }
    @GetMapping("/winestyle")
    public List<Wine> wineStyleDiscountedWines() {
        return new WineStyle().getAllWines();
    }
    @GetMapping("/winehelp")
    public List<Wine> wineHelpDiscountedWines() {
        return new WineHelp().getAllWines();
    }
    @GetMapping("/luding")
    public List<Wine> ludingDiscountedWines() {
        return new Luding().getAllWines();
    }
    @GetMapping("/amwine")
    public List<Wine> amWineDiscountedWines() {
        return new AmWine().getAllWines();
    }
}