package com.project.search;

import com.project.search.service.SearchService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by tony.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Configuration
public class SearchServiceTests  {

    @Autowired
    private SearchService searchService;

    @Test
    public void testIndex() {
        int targetHouseId = 15;
        searchService.index(targetHouseId);
    }

    @Test
    public void testRemove() {
        String targetHouseId = "15";
        searchService.remove(targetHouseId);
    }

//    @Test
//    public void testQuery() {
//        RentSearch rentSearch = new RentSearch();
//        rentSearch.setCityEnName("bj");
//        rentSearch.setStart(0);
//        rentSearch.setSize(10);
//        rentSearch.setKeywords("国贸");
//        ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
//        Assert.assertTrue(serviceResult.getTotal() > 0);
//    }
}
