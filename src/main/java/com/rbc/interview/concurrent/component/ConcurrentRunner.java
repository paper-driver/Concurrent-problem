package com.rbc.interview.concurrent.component;

import com.rbc.interview.concurrent.controller.IndexController;
import com.rbc.interview.concurrent.model.Index;
import com.rbc.interview.concurrent.model.StockSymbol;
import com.rbc.interview.concurrent.payload.ResponseMessage;
import com.rbc.interview.concurrent.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ConcurrentRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentRunner.class);

    @Autowired
    IndexService indexService;

    @Autowired
    IndexController indexController;

    @Value(value = "classpath:dow_jones_index-part1.csv")
    private Resource data1;

    @Value(value = "classpath:dow_jones_index-part2.csv")
    private Resource data2;

    @Override
    public void run(String... arg) throws Exception {
        Instant startTimer = Instant.now();

        logger.info("Checking Update Concurrency =====");
        List<CompletableFuture<ResponseMessage>> allUpdates = new ArrayList<>();
        Double openPrice = 2.0;
        Long time = 0L;
        //call the function multiple time instantly
        for(int i = 0; i < 3; i++){
            // find stock information for AA
            Index index1 = new Index();
            index1.setQuarter(1);
            index1.setStock(StockSymbol.AA);
            Date date = new Date(time);
            index1.setDate(date);
            index1.setOpen(openPrice);
            allUpdates.add(indexService.updateIndex(index1));
            openPrice ++;
            time  = time + 1000000000000L;
        }

        CompletableFuture.allOf(allUpdates.toArray(new CompletableFuture[0])).join();

        for(int i = 0; i < 3; i++){
            System.out.println("response: " + allUpdates.get(i).get().getMessage());
        }

        System.out.println("Total time: " + Duration.between(startTimer, Instant.now()).getSeconds());

        startTimer = Instant.now();
        logger.info("Checking Get Concurrency =====");
        List<CompletableFuture<ResponseEntity>> allGets = new ArrayList<>();

        //call the function multiple time instantly
        for(int i = 0; i < 10; i++){
            // find stock information for AA
            allGets.add(indexController.getByStock(StockSymbol.AA));
        }

        CompletableFuture.allOf(allGets.toArray(new CompletableFuture[0])).join();

        for(int i = 0; i < 10; i++){
            System.out.println("response: " + allGets.get(i).get().getStatusCode());
        }

        System.out.println("Total time: " + Duration.between(startTimer, Instant.now()).getSeconds());

        logger.info("Checking Upload Concurrency =====");
        startTimer = Instant.now();
        List<CompletableFuture<ResponseMessage>> allUploads = new ArrayList<>();


        InputStream input1 = data1.getInputStream();
        InputStream input2 = data2.getInputStream();
        InputStream input3 = data1.getInputStream();

        //call the function multiple time instantly
        allUploads.add(indexService.addIndexes(input1));
        allUploads.add(indexService.addIndexes(input2));
        allUploads.add(indexService.addIndexes(input3));

        CompletableFuture.allOf(allUploads.toArray(new CompletableFuture[0])).join();

        for(int i = 0; i < 3; i++){
            System.out.println("response: " + allUploads.get(i).get().getMessage());
        }

        System.out.println("Total time: " + Duration.between(startTimer, Instant.now()).getSeconds());

    }
}
