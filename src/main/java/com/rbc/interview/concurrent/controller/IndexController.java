package com.rbc.interview.concurrent.controller;

import com.rbc.interview.concurrent.model.Index;
import com.rbc.interview.concurrent.model.StockSymbol;
import com.rbc.interview.concurrent.payload.ResponseMessage;
import com.rbc.interview.concurrent.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import java.util.function.Function;

@RestController
@RequestMapping("/api/index")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private IndexService indexService;

    @RequestMapping(value = "/upload", method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ResponseMessage>> uploadFile(@RequestParam(value = "files") MultipartFile[] files){
        List<ResponseMessage> responseMessages = new ArrayList<>();
        try {
            for(final MultipartFile file : files){
                indexService.addIndexes(file.getInputStream()).thenApply(responseMessages::add);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMessages);
        } catch(final Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessages);
        }
    }

    @RequestMapping(value = "/getbystock", method = RequestMethod.GET,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompletableFuture<ResponseEntity> getByStock(@RequestParam(value = "stock") StockSymbol stock){
        return indexService.getInfoOfAStock(stock).<ResponseEntity>thenApply(ResponseEntity::ok)
                .exceptionally(handleGetIndexException);
    }

    private static Function<Throwable, ResponseEntity<? extends List<Index>>> handleGetIndexException = throwable -> {
        logger.error("Failed to get records: []", throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    };

    @RequestMapping(value = "/update", method = RequestMethod.POST,
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public CompletableFuture<ResponseEntity> upldateARecord(@Valid @RequestBody Index index){
        return indexService.updateIndex(index).<ResponseEntity>thenApply(result -> new ResponseEntity<ResponseMessage>(result, result.getStatus()));
    }

}
