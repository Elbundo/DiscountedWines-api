package com.elbundo.DiscountedWinesapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Wine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String site;
    private String page;
    private String title;
    private String alias;
    private double price;
    private double priceWithDiscount;
    private double discount;
    private String pathImage;
    private String ratings;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wine wine = (Wine) o;
        return Double.compare(wine.price, price) == 0 && Double.compare(wine.priceWithDiscount, priceWithDiscount) == 0 && Double.compare(wine.discount, discount) == 0 && site.equals(wine.site) && page.equals(wine.page) && title.equals(wine.title) && alias.equals(wine.alias) && pathImage.equals(wine.pathImage) && ratings.equals(wine.ratings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, page, title, alias, price, priceWithDiscount, discount, pathImage, ratings);
    }
}
