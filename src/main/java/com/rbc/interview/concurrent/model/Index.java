package com.rbc.interview.concurrent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Data
@Table( name = "index_table",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"stock", "date"})
        })
public class Index implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column (name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // this is required to apply optimistic locking
    @Version
    private Integer version;

    @NotNull
    private Integer quarter;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StockSymbol stock;

    @NotNull
    private Date date;

    @NotNull
    private Double open = 0.0;

    @NotNull
    private Double high = 0.0;

    @NotNull
    private Double low = 0.0;

    @NotNull
    private Double close = 0.0;

    @NotNull
    private Long volume = 0L;

    @NotNull
    private Double percent_change_price = 0.0;

    @NotNull
    private Double percent_change_volume_over_last_wek = 0.0;

    @NotNull
    private Long previous_weeks_volume = 0L;

    @NotNull
    private Double next_weeks_open = 0.0;

    @NotNull
    private Double next_weeks_close = 0.0;

    @NotNull
    private Double percent_change_next_weeks_price = 0.0;

    @NotNull
    private Double percent_return_next_dividend = 0.0;

    public Index() {}

    public Index(Integer quarter, StockSymbol stock, Date date){
        this.quarter = quarter;
        this.stock = stock;
        this.date = date;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public StockSymbol getStock() {
        return stock;
    }

    public void setStock(StockSymbol stock) {
        this.stock = stock;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Double getPercent_change_price() {
        return percent_change_price;
    }

    public void setPercent_change_price(Double percent_change_price) {
        this.percent_change_price = percent_change_price;
    }

    public Double getPercent_change_volume_over_last_wek() {
        return percent_change_volume_over_last_wek;
    }

    public void setPercent_change_volume_over_last_wek(Double percent_change_volume_over_last_wek) {
        this.percent_change_volume_over_last_wek = percent_change_volume_over_last_wek;
    }

    public Double getNext_weeks_open() {
        return next_weeks_open;
    }

    public void setNext_weeks_open(Double next_weeks_open) {
        this.next_weeks_open = next_weeks_open;
    }

    public Double getNext_weeks_close() {
        return next_weeks_close;
    }

    public void setNext_weeks_close(Double next_weeks_close) {
        this.next_weeks_close = next_weeks_close;
    }

    public Double getPercent_change_next_weeks_price() {
        return percent_change_next_weeks_price;
    }

    public void setPercent_change_next_weeks_price(Double percent_change_next_weeks_price) {
        this.percent_change_next_weeks_price = percent_change_next_weeks_price;
    }

    public Double getPercent_return_next_dividend() {
        return percent_return_next_dividend;
    }

    public void setPercent_return_next_dividend(Double percent_return_next_dividend) {
        this.percent_return_next_dividend = percent_return_next_dividend;
    }

    public Long getPrevious_weeks_volume() {
        return previous_weeks_volume;
    }

    public void setPrevious_weeks_volume(Long previous_weeks_volume) {
        this.previous_weeks_volume = previous_weeks_volume;
    }
}
