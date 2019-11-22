package com.project.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.project.search.common.exception.BusinessException;
import com.project.search.common.utils.StringUtils;
import com.project.search.common.utils.TransferOrderUtil;
import com.project.search.dao.mappers.HouseDetailMapper;
import com.project.search.dao.mappers.HouseMapper;
import com.project.search.dao.mappers.HouseTagMapper;
import com.project.search.dao.model.*;
import com.project.search.entity.es.HouseIndexKey;
import com.project.search.entity.es.HouseIndexTemplate;
import com.project.search.entity.es.HouseSuggest;
import com.project.search.entity.param.RentSearch;
import com.project.search.entity.param.RentValueBlock;
import com.project.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService{
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private static final String INDEX_NAME = "xunwu";

    private static final String INDEX_TYPE = "house";

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private TransportClient esClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void index(int houseId) {
        ModelMapper modelMapper = new ModelMapper();
        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();
        //查找出对应的house信息
        HouseExample houseExample = new HouseExample();
        houseExample.createCriteria().andIdEqualTo(houseId);
        List<House> houseList = houseMapper.selectByExample(houseExample);
        if (houseList.size() == 0 ){
            throw new BusinessException("未查到房屋信息");
        }
        House house = houseList.get(0);

        modelMapper.map(house,houseIndexTemplate);
        //查出对应的house detail
        HouseDetailExample houseDetailExample = new HouseDetailExample();
        houseDetailExample.createCriteria().andHouseIdEqualTo(houseId);
        List<HouseDetail> houseDetailList = houseDetailMapper.selectByExample(houseDetailExample);
        if (houseDetailList.size() == 0){
            throw new BusinessException("未找到房屋详细信息");
        }
        HouseDetail houseDetail = houseDetailList.get(0);

        modelMapper.map(houseDetail,houseIndexTemplate);
        //查出对应house tag信息
        HouseTagExample houseTagExample = new HouseTagExample();
        houseTagExample.createCriteria().andHouseIdEqualTo(houseId);
        List<HouseTag> houseTags = houseTagMapper.selectByExample(houseTagExample);
        if (houseTags.size() > 0 ){
            List<String> tagStrings = new ArrayList<>();
            houseTags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
            houseIndexTemplate.setTags(tagStrings);
        }

        // 在es中查询对应house id的记录
        SearchRequestBuilder requestBuilder = esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,String.valueOf(houseId)));
        SearchResponse response = requestBuilder.get();

        Long totalHit = response.getHits().getTotalHits();

        //如果查询出来的数据等于0创建新的,如果等于1就更新,否则脏数据删除index后创建
        if (totalHit == 0) {
            createIndex(houseIndexTemplate);
        } else if (totalHit == 1) {
            String esId = response.getHits().getAt(0).getId();
            updateIndex(houseIndexTemplate,esId);
        } else {
            deleteAndCreateIndex(totalHit, houseIndexTemplate);
        }

    }

    @Override
    public void remove(String houseId) {
        boolean isSuccess = true;
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.
                newRequestBuilder(esClient).
                filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId)).
                source(INDEX_NAME);

        BulkByScrollResponse response = builder.get();
        Long deleteCount = response.getDeleted();
        log.info("删除的index {} 条",deleteCount);
    }

    @Override
    public List<Integer> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        //过滤房屋名称
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME,rentSearch.getCityEnName()));

        //过滤区域名称
        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }

        //此处查出面积的区间，如果不是所有面积，就筛选条件小于最大值 大于最小值
        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        //筛选价格区间
        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                rangeQuery.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQuery.gte(price.getMin());
            }
            boolQuery.filter(rangeQuery);
        }

        //筛选朝向
        if (rentSearch.getDirection() > 0) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection())
            );
        }

        //筛选租房方式
        if (rentSearch.getRentWay() > -1) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay())
            );
        }

        //多匹配模式
        boolQuery.must(
                QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                        HouseIndexKey.TITLE,
                        HouseIndexKey.TRAFFIC,
                        HouseIndexKey.DISTRICT,
                        HouseIndexKey.ROUND_SERVICE,
                        HouseIndexKey.SUBWAY_LINE_NAME,
                        HouseIndexKey.SUBWAY_STATION_NAME
                ));

        //映射筛选条件
        rentSearch.setOrderBy(new TransferOrderUtil().getEsOrderBy(rentSearch.getOrderBy()));
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)
                .addSort(
                        rentSearch.getOrderBy(),
                    SortOrder.fromString(rentSearch.getOrderDirection())
                )
                .setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize())
                .setFetchSource(HouseIndexKey.HOUSE_ID, null);//获取指定字段

        log.info("query es :" + searchRequestBuilder);
        List<Integer> houseIds = new ArrayList<>();

        SearchResponse searchResponse = searchRequestBuilder.get();
        if (searchResponse.status() != RestStatus.OK){
            return houseIds;
        }

        for (SearchHit hit : searchResponse.getHits()) {
            houseIds.add(Integer.parseInt(hit.getSource().get(HouseIndexKey.HOUSE_ID).toString()));
        }
        return houseIds;
    }

    @Override
    public List<String> suggest(String prefix) {
        List<String> result = new ArrayList<>();
        //生成搜索提示的构造器,completionSuggestion里面放的是提示的数组名，size是大小
        CompletionSuggestionBuilder completionSuggestionBuilder =
                SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);

        //把搜索的限定条件放入构造器，作为查询依据
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("autoCompletion",completionSuggestionBuilder);

        //通过es进行查询,获取查询出来的suggest
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE).suggest(suggestBuilder);

        SearchResponse response = searchRequestBuilder.get();
        Suggest suggestions = response.getSuggest();

        if (suggestions == null){
            return result;
        }

        //获取之前设置的autoCompletion，主要是获取Suggest.Suggestion这个list里面的CompletionSuggestion.Entry.option里面text
        Suggest.Suggestion autoCompletion = suggestions.getSuggestion("autoCompletion");

        int maxSuggest = 0;
        Set<String> suggestSet = new HashSet<>();

        for (Object object:autoCompletion.getEntries()) {
            if (object instanceof CompletionSuggestion.Entry){
                CompletionSuggestion.Entry entry = (CompletionSuggestion.Entry)object;

                if (entry.getOptions().isEmpty()){
                    continue;
                }

                for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                    String tip = option.getText().string();
                    if (suggestSet.contains(tip)) {
                        continue;
                    }
                    suggestSet.add(tip);
                    maxSuggest++;
                }
            }
            //最多提示5个
            if (maxSuggest > 5) {
                break;
            }
        }

        result = Lists.newArrayList(suggestSet.toArray(new String[]{}));
        return result;
    }

    @Override
    public Long aggregateDistrictHouse(String cityName, String regionName, String district) {
        //首先设置筛选条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME,cityName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME,regionName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT,district));

        //查询数据，同时设置搜索的聚合名称和要放入字段
        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(boolQueryBuilder)
                .addAggregation(AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT).field(HouseIndexKey.DISTRICT)).setSize(0);

        SearchResponse searchResponse = searchRequestBuilder.get();

        if (searchResponse.status() == RestStatus.OK) {
            //从结果获取聚合查询的数据集并取出字段，统计数量
            Terms terms = searchResponse.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
            if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
                return terms.getBucketByKey(district).getDocCount();
            }
        } else {
            logger.warn("Failed to Aggregate for " + HouseIndexKey.AGG_DISTRICT);

        }
        return 0L;
    }


    //------------------------------------------
    /**
     * 创建house的index标签
     * @param houseIndexTemplate
     * @return
     */
    private boolean createIndex(HouseIndexTemplate houseIndexTemplate){
        boolean isSuccess = true;
        if (!updateSuggest(houseIndexTemplate)) {
            return false;
        }

        try{
            //prepareIndex用来指定es的哪个mapping哪个index
            //setSource用来把对象转为byte和json存入es
             IndexResponse response= esClient.prepareIndex(INDEX_NAME,INDEX_TYPE).
                    setSource(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();
            if (response.status() != RestStatus.CREATED) {
                isSuccess = false;
            }
            log.info("生成index{} house标签成功",houseIndexTemplate.getHouseId());
        } catch (Exception e){
            log.error("生成index house标签失败");
        }
        return isSuccess;
    }

    /**
     * 更新house的index标签
     * @param houseIndexTemplate
     * @param houseId
     * @return
     */
    private boolean updateIndex(HouseIndexTemplate houseIndexTemplate,String houseId){
        boolean isSuccess = true;
        if (!updateSuggest(houseIndexTemplate)) {
            return false;
        }

        try{
            UpdateResponse response = esClient.prepareUpdate(INDEX_NAME,INDEX_TYPE,houseId).
                    setDoc(objectMapper.writeValueAsBytes(houseIndexTemplate), XContentType.JSON).get();
            if (response.status() != RestStatus.OK){
                isSuccess = false;
            }
            log.info("update index {} house标签成功",houseIndexTemplate.getHouseId());
        } catch (Exception e){
            log.error("更新index house标签失败");
        }
        return isSuccess;
    }

    /**
     * 删除 house的index标签
     * @param houseIndexTemplate
     * @param houseIndexTemplate
     * @return
     */
    private boolean deleteAndCreateIndex(Long totalCount,HouseIndexTemplate houseIndexTemplate){
        boolean isSuccess = true;
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE.
                newRequestBuilder(esClient).
                filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseIndexTemplate.getHouseId())).
                source(INDEX_NAME);

        BulkByScrollResponse response = builder.get();
        Long deleteCount = response.getDeleted();
        if (deleteCount != totalCount){
            log.info("删除的index条目和实际的不匹配,未删除完整");
            isSuccess = false;
        } else {
            isSuccess = createIndex(houseIndexTemplate);
            log.info("删除index {} house标签成功",houseIndexTemplate.getHouseId());
        }
        return isSuccess;
    }

    /**
     * 更新房屋的suggest补全信息
     * @param indexTemplate
     * @return
     */
    private boolean updateSuggest(HouseIndexTemplate indexTemplate){
        //设置需补全的字段
        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
                esClient, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
                indexTemplate.getSubwayStationName());

        requestBuilder.setAnalyzer("ik_smart");

        AnalyzeResponse response = requestBuilder.get();
        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
        if (tokens == null) {
            logger.warn("Can not analyze token for house: " + indexTemplate.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = new ArrayList<>();
        //tokens类似于分词后的结果，通过把这些结果排除数字和长度小于2的放入suggest，之后自动补全的时候就能获取
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            // 排序数字类型 & 小于2个字符的分词结果
            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                continue;
            }

            HouseSuggest suggest = new HouseSuggest();
            suggest.setInput(token.getTerm());
            suggests.add(suggest);
        }

        // 定制化小区自动补全
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(indexTemplate.getDistrict());
        suggests.add(suggest);

        indexTemplate.setSuggest(suggests);
        return true;
    }
}
