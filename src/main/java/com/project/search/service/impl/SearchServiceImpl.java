package com.project.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.project.search.entity.param.RentSearch;
import com.project.search.entity.param.RentValueBlock;
import com.project.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        boolean isSuccess = true;
        if (totalHit == 0) {
            isSuccess = createIndex(houseIndexTemplate);
        } else if (totalHit == 1) {
            String esId = response.getHits().getAt(0).getId();
            isSuccess = updateIndex(houseIndexTemplate,esId);
        } else {
            isSuccess = deleteAndCreateIndex(totalHit, houseIndexTemplate);
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


    //------------------------------------------
    /**
     * 创建house的index标签
     * @param houseIndexTemplate
     * @return
     */
    private boolean createIndex(HouseIndexTemplate houseIndexTemplate){
        boolean isSuccess = true;
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
}
