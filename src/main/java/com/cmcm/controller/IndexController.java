package com.cmcm.controller;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Spontaneously
 * @time 2018-08-05 下午3:43
 */
@RestController
public class IndexController {

    @Autowired
    private TransportClient client;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/get/book/novel")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name = "id", defaultValue = "") String id) {
        if (StringUtils.isEmpty(id)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        GetResponse response = client.prepareGet("book", "novel", id).get();

        if (!response.isExists()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(response.getSource(), HttpStatus.OK);
    }

    @PostMapping("add/book/novel")
    @ResponseBody
    public ResponseEntity add(
            @RequestParam(name = "title") String title, @RequestParam(name = "author") String author, @RequestParam(name = "word_count") int wordCount,
            @RequestParam(name = "publish_date") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishDate) {
        try {
            XContentBuilder content = XContentFactory.jsonBuilder().startObject()
                    .field("title", title)
                    .field("author", author)
                    .field("word_count", wordCount)
                    .field("publish_date", publishDate)
                    .endObject();

            IndexResponse indexResponse = this.client.prepareIndex("book", "novel").setSource(content).get();
            return new ResponseEntity(indexResponse.getId(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("delete/book/novel")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(name = "id") String id) {
        DeleteResponse deleteResponse = this.client.prepareDelete("book", "novel", id).get();
        return new ResponseEntity(deleteResponse.getResult().toString(), HttpStatus.OK);
    }

    @PutMapping("update/book/novel")
    @ResponseBody
    public ResponseEntity update(@RequestParam(name = "id") String id, @RequestParam(name = "title", required = false) String title,
                                 @RequestParam(name = "author", required = false) String author, @RequestParam(name = "word_count", required = false) int wordCount,
                                 @RequestParam(name = "publish_date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishDate) {

        UpdateRequest update = new UpdateRequest("book", "novel", id);
        XContentBuilder builder = null;
        try {
            builder = XContentFactory.jsonBuilder().startObject();

            if (!StringUtils.isEmpty(title)) {
                builder.field("title", title);
            }
            if (!StringUtils.isEmpty(author)) {
                builder.field("author", author);
            }

            builder.endObject();

            update.doc(builder);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            UpdateResponse response = this.client.update(update).get();
            return new ResponseEntity(response.getResult().toString(), HttpStatus.OK);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("query/book/novel")
    @ResponseBody
    public ResponseEntity query(
            @RequestParam(name = "author", required = false) String author, @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "gt_count", defaultValue = "0") Integer gtCount, @RequestParam(name = "lt_count", required = false) Integer ltCount) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(author)) {
            boolQuery.must(QueryBuilders.matchQuery("author", author));
        }

        if (!StringUtils.isEmpty(title)) {
            boolQuery.must(QueryBuilders.matchQuery("title", title));
        }

        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("word_count").from(gtCount);
        if (ltCount != null && ltCount > gtCount) {
            rangeQuery.to(ltCount);
        }

        boolQuery.filter(rangeQuery);

        SearchRequestBuilder builder = this.client.prepareSearch("book").setTypes("novel").setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQuery).setFrom(0).setSize(100);

        System.out.println(builder);

        SearchResponse response = builder.get();

        List<Map<String, Object>> result = new ArrayList<>();

        for (SearchHit hit : response.getHits()) {
            result.add(hit.getSource());
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
