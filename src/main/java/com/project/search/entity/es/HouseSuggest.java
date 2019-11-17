package com.project.search.entity.es;

import lombok.Data;

@Data
public class HouseSuggest {
    private String input;

    private int weight = 10;
}
