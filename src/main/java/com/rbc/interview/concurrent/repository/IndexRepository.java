package com.rbc.interview.concurrent.repository;

import com.rbc.interview.concurrent.model.Index;
import com.rbc.interview.concurrent.model.StockSymbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository()
public interface IndexRepository extends JpaRepository<Index, Long> {

    @Query(value = "SELECT u FROM Index u WHERE u.stock = :stock")
    List<Index> findByStock(@Param("stock") StockSymbol stock);

    Optional<Index> findById(Long id);

    @Query(value = "SELECT u FROM Index u WHERE u.stock = :stock AND u.date = :date")
    Index findByStockAndDate(@Param("stock") StockSymbol stock, @Param("date") Date date);

    Boolean existsByStockAndDate(StockSymbol stock, Date date);

    @Modifying
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query(value = "UPDATE Index u SET u.open = :open WHERE u.stock = :stock AND u.date = :date")
    void updateiIndex(@Param("open") Double open, @Param("stock") StockSymbol stock, @Param("date") Date date);

}
