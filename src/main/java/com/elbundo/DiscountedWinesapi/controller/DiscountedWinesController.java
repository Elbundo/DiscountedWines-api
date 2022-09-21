package com.elbundo.DiscountedWinesapi.controller;

import com.elbundo.DiscountedWinesapi.handlers.*;
import com.elbundo.DiscountedWinesapi.handlers.Parsers.AbstractParser;
import com.elbundo.DiscountedWinesapi.model.Wine;
import com.elbundo.DiscountedWinesapi.repository.WineRepository;
import com.elbundo.DiscountedWinesapi.service.DiscountedWinesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/wines")
public class DiscountedWinesController {
    private final DiscountedWinesService wineService;

    public DiscountedWinesController(DiscountedWinesService service) {
        this.wineService = service;
    }
    @GetMapping("/discounted")
    public List<Wine> discountedWines() {
        return wineService.discountedWines();
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