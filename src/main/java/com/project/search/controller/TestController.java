package com.project.search.controller;

import com.project.search.common.utils.ResultHelper;
import com.project.search.config.interf.PassPermission;
import com.project.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PassPermission(value = "")
public class TestController {
    @Autowired
    SearchService searchService;

    @GetMapping("/es/index")
    public Object index(@RequestParam int houseId){
        searchService.index(houseId);
        return new ResultHelper();
    }

    @GetMapping("/es/remove")
    public Object index(@RequestParam String houseId){
        searchService.remove(houseId);
        return new ResultHelper();
    }
}
