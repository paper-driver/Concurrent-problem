package com.rbc.interview.concurrent.service;

import com.rbc.interview.concurrent.model.Index;
import com.rbc.interview.concurrent.model.StockSymbol;
import com.rbc.interview.concurrent.payload.ResponseMessage;
import com.rbc.interview.concurrent.repository.IndexRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
//import javax.transaction.Transactional;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexService {

    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private IndexRepository indexRepository;

    @Async
    public CompletableFuture<ResponseMessage> addIndexes(final InputStream inputStream) throws Exception {
        final long startTimer = System.currentTimeMillis();

        List<Index> indexes = parseCSVFile(inputStream);

//        //check for duplicate
//        for(Index index : indexes){
//            if(indexRepository.existsByStockAndDate(index.getStock(), index.getDate())){
//                indexes.remove(index);
//            }
//        }

        logger.info("Saving a list of indexes of size {} records", indexes.size());

//        indexes = indexRepository.saveAll(indexes);
         ResponseMessage responseMessage = insertTransaction(indexes);

        logger.info("Elapsed time: {}", (System.currentTimeMillis() - startTimer));
        return CompletableFuture.completedFuture(responseMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Index> parseCSVFile(final InputStream inputStream) throws Exception {
        final List<Index> indexes = new ArrayList<>();

        try {
            try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = buffer.readLine()) != null ){
                    logger.info("creating index of = " + line);
                    final String[] data = line.split(",");
                    if(!data[0].toLowerCase().equals("quarter")){
                        // assume we are not at the column name row. There might be better solution to validate it.
                        final Index index = new Index();
                        if(!data[0].isEmpty() && !data[1].isEmpty() && !data[2].isEmpty()){
                            index.setQuarter(Integer.parseInt(data[0]));
                            StockSymbol symbol;
                            try {
                                symbol = StockSymbol.valueOf(data[1]);
                                index.setStock(symbol);
                            } catch (IllegalArgumentException ex) {
                                // not valid stock symbol
                                logger.error("Invalid Stock name!", ex);
                                throw new Exception("Need a valid Stock name!", ex);
                            };
                            SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
                            index.setDate(new java.sql.Date(date.parse(data[2]).getTime()));
                            index.setOpen(data[3].isEmpty() ? 0.0 : (data[3].charAt(0) == '$' ? Double.parseDouble(data[3].substring(1)) : Double.parseDouble(data[3])));
                            index.setHigh(data[4].isEmpty() ? 0.0 : (data[4].charAt(0) == '$' ? Double.parseDouble(data[4].substring(1)) : Double.parseDouble(data[4])));
                            index.setLow(data[5].isEmpty() ? 0.0 : (data[5].charAt(0) == '$' ? Double.parseDouble(data[5].substring(1)) : Double.parseDouble(data[5])));
                            index.setClose(data[6].isEmpty() ? 0.0 : (data[6].charAt(0) == '$' ? Double.parseDouble(data[6].substring(1)) : Double.parseDouble(data[6])));
                            index.setVolume(data[7].isEmpty() ? 0L : Long.parseLong(data[7]));
                            index.setPercent_change_price(data[8].isEmpty() ? 0.0 : Double.parseDouble(data[8]));
                            index.setPercent_change_volume_over_last_wek(data[9].isEmpty() ? 0.0 : Double.parseDouble(data[9]));
                            index.setPrevious_weeks_volume(data[10].isEmpty() ? 0L : Long.parseLong(data[10]));
                            index.setNext_weeks_open(data[11].isEmpty() ? 0.0 : (data[11].charAt(0) == '$' ? Double.parseDouble(data[11].substring(1)) : Double.parseDouble(data[11])));
                            index.setNext_weeks_close(data[12].isEmpty() ? 0.0 : (data[12].charAt(0) == '$' ? Double.parseDouble(data[12].substring(1)) : Double.parseDouble(data[12])));
                            index.setPercent_change_next_weeks_price(data[13].isEmpty() ? 0.0 : Double.parseDouble(data[13]));
                            index.setPercent_return_next_dividend(data[14].isEmpty() ? 0.0 : Double.parseDouble(data[14]));
                            if(!indexRepository.existsByStockAndDate(index.getStock(), index.getDate())){
                                indexes.add(index);
                            }

                        }else{
                            // return in valid request message to consumer
                            logger.error("Invalid data!");
                            throw new Exception("Need Quarter, Stock, and Date information for each row!");
                        }
                    }
                }

                return indexes;
            }
        } catch(final IOException e) {
            logger.error("Failed to parse CSV file []", e);
            throw new Exception("Failed to parse CSV file []", e);
        }
    }

    @Async
    public CompletableFuture<List<Index>> getInfoOfAStock(StockSymbol stock){
        logger.info("Request to get a list of information of a specific stock");

        final List<Index> indexes = indexRepository.findByStock(stock);
        return CompletableFuture.completedFuture(indexes);

    }

    @Async
    public CompletableFuture<ResponseMessage> updateIndex(Index index) {
        return CompletableFuture.completedFuture(updateTransaction(index));
    }


    /**
     * This way, database is manged with one shared transaction to avoid any conflicts.
     * @param indexes
     * @return
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public ResponseMessage insertTransaction(List<Index> indexes){
        try {
            for(Index index : indexes){
                if(indexRepository.existsByStockAndDate(index.getStock(), index.getDate())){
                    indexes.remove(index);
                }
            }
            indexRepository.saveAll(indexes);
            return new ResponseMessage("Succeeded", HttpStatus.CREATED);
        } catch(Exception e) {
            logger.error("insert error : " + e.getMessage());
            return new ResponseMessage("Some user is inserting same data", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public ResponseMessage updateTransaction(Index index){
        if(indexRepository.existsByStockAndDate(index.getStock(), index.getDate())){
            try{
                Index existing_Index = indexRepository.findByStockAndDate(index.getStock(), index.getDate());
                existing_Index.setOpen(index.getOpen());
                existing_Index.setHigh(index.getHigh());
                existing_Index.setLow(index.getLow());
                existing_Index.setClose(index.getClose());
                existing_Index.setVolume(index.getVolume());
                existing_Index.setPercent_return_next_dividend(index.getPercent_return_next_dividend());
                existing_Index.setPercent_change_next_weeks_price(index.getPercent_change_next_weeks_price());
                existing_Index.setPercent_change_price(index.getPercent_change_price());
                existing_Index.setPercent_change_volume_over_last_wek(index.getPercent_change_volume_over_last_wek());
                existing_Index.setPrevious_weeks_volume(index.getPrevious_weeks_volume());
                existing_Index.setNext_weeks_close(index.getNext_weeks_close());
                existing_Index.setNext_weeks_open(index.getNext_weeks_open());
                indexRepository.save(existing_Index);
                return new ResponseMessage("Succeeded", HttpStatus.CREATED);
            } catch(Exception e) {
                logger.error("update error : " + e.getMessage());
                return new ResponseMessage("Some user is updating the same record", HttpStatus.CONFLICT);
            }

//            indexRepository.updateiIndex(index.getOpen(), index.getStock(), index.getDate());
        }else{
            try{
                indexRepository.save(index);
                return new ResponseMessage("Succeeded", HttpStatus.CREATED);
            } catch(Exception e) {
                logger.error("update error : " + e.getMessage());
                return new ResponseMessage("Some user is insert the same record as you do", HttpStatus.ALREADY_REPORTED);
            }

        }

    }


}
