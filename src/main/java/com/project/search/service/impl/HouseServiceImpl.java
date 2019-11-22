package com.project.search.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.project.search.common.enums.HouseStatus;
import com.project.search.common.exception.BusinessException;
import com.project.search.common.utils.JsonMapper;
import com.project.search.common.utils.ResultHelper;
import com.project.search.common.utils.StringUtils;
import com.project.search.common.utils.TransferOrderUtil;
import com.project.search.config.rabbitmq.MqSender;
import com.project.search.constants.MqConstants;
import com.project.search.dao.mappers.*;
import com.project.search.dao.model.*;
import com.project.search.entity.dto.HouseDTO;
import com.project.search.entity.dto.HouseDetailDTO;
import com.project.search.entity.dto.HousePictureDTO;
import com.project.search.entity.param.DataTableSearch;
import com.project.search.entity.param.HouseForm;
import com.project.search.entity.param.PhotoForm;
import com.project.search.entity.param.RentSearch;
import com.project.search.service.HouseService;
import com.project.search.service.SearchService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class HouseServiceImpl implements HouseService {
    @Value("${fastdfs.url}")
    private String picUrl;

    @Autowired
    private SupportAddressMapper supportAddressMapper;

    @Autowired
    private SubwayMapper subwayMapper;

    @Autowired
    private SubwayStationMapper subwayStationMapper;

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private HousePictureMapper housePictureMapper;

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseExtMapper houseExtMapper;

    @Autowired
    private HouseSubscribeMapper houseSubscribeMapper;

    @Autowired
    private SearchService searchService;

    @Autowired
    private MqSender mqSender;

    @Override
    public Object getSupportAddress() {
        SupportAddressExample example = new SupportAddressExample();
        example.createCriteria().andLevelEqualTo("city");
        List<SupportAddress> supportAddressList = supportAddressMapper.selectByExample(example);
        return supportAddressList;
    }

    @Override
    public Object getSupportAddress(String cityName) {
        SupportAddressExample example = new SupportAddressExample();
        example.createCriteria().andLevelEqualTo("city").andEnNameEqualTo(cityName);
        List<SupportAddress> supportAddressList = supportAddressMapper.selectByExample(example);
        return supportAddressList;
    }

    @Override
    public Object getSupportRegions(String cityName) {
        SupportAddressExample example = new SupportAddressExample();
        example.createCriteria().andLevelEqualTo("region").andBelongToEqualTo(cityName);
        List<SupportAddress> supportAddressList = supportAddressMapper.selectByExample(example);
        return supportAddressList;
    }

    @Override
    public Object getSupportSubwayLine(String cityName) {
        SubwayExample example = new SubwayExample();
        example.createCriteria().andCityEnNameEqualTo(cityName);
        List<Subway> subwayList = subwayMapper.selectByExample(example);
        return subwayList;
    }

    @Override
    public Object getSubwayStation(int subwayId) {
        SubwayStationExample example = new SubwayStationExample();
        example.createCriteria().andSubwayIdEqualTo(subwayId);
        List<SubwayStation> subwayStationList = subwayStationMapper.selectByExample(example);
        return subwayStationList;
    }

    @Override
    @Transactional
    public Object addHouseInfo(HouseForm houseForm) {
        ModelMapper modelMapper = new ModelMapper();
        // 插入house
        House house = new House();
        modelMapper.map(houseForm,house);

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setStatus(0);
        //TODO 此处应该从切面获取用户的信息
        house.setAdminId(1);
        houseMapper.insert(house);

        // 插入housedetail
        HouseDetail houseDetail = new HouseDetail();
        wrapperDetailInfo(houseDetail,houseForm);
        houseDetail.setHouseId(house.getId());
        houseDetailMapper.insert(houseDetail);

        // 插入housePic
        //TODO 此处应该用mybatis的循环插入
        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        for (HousePicture pic:pictures) {
            housePictureMapper.insert(pic);
        }

        // 插入houseTag
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        pictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(picUrl + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        List<HouseTag> houseTags = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseDTO.setTags(tags);
        }

        //TODO 此处应该循环插入
        for (HouseTag houseTag:houseTags) {
            houseTagMapper.insert(houseTag);
        }

        return new ResultHelper().newSuccessResult(houseDTO);
    }

    @Override
    public Object getHouseLists(DataTableSearch searchBody) {
        int page = searchBody.getStart() / searchBody.getLength();

        PageHelper.startPage(page + 1,searchBody.getLength());

        List<House> houseList = houseExtMapper.getHouseList(searchBody);

        return new PageInfo(houseList);
    }

    @Override
    public Object getIndexHouseLists(RentSearch rentSearch) {
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()){
            List<Integer> houseIds = searchService.query(rentSearch);
            if (houseIds.size() == 0){
                PageInfo pageInfo = new PageInfo();
                pageInfo.setList(new ArrayList());
                return pageInfo;
            }
            return getHouseListByEs(houseIds);
        }
        return getHouseListsNormal(rentSearch);
    }

    @Override
    public Object getHouseAllInfo(Long houseId) {
        ModelMapper modelMapper = new ModelMapper();
        //获取房屋信息
        HouseExample houseExample = new HouseExample();
        houseExample.createCriteria().andIdEqualTo(houseId.intValue());
        House house = houseMapper.selectByExample(houseExample).get(0);
        if (house == null) {
            throw new BusinessException("未找到对应房屋!");
        }

        //获取房屋详细信息
        HouseDetailExample houseDetailExample = new HouseDetailExample();
        houseDetailExample.createCriteria().andHouseIdEqualTo(houseId.intValue());
        HouseDetail detail = houseDetailMapper.selectByExample(houseDetailExample).get(0);

        //获取房屋图片信息
        HousePictureExample housePictureExample = new HousePictureExample();
        housePictureExample.createCriteria().andHouseIdEqualTo(houseId.intValue());
        List<HousePicture> housePictureList = housePictureMapper.selectByExample(housePictureExample);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : housePictureList) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }

        //获取房屋的tag
        HouseTagExample houseTagExample = new HouseTagExample();
        houseTagExample.createCriteria().andHouseIdEqualTo(houseId.intValue());
        List<HouseTag> tags = houseTagMapper.selectByExample(houseTagExample);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        //查找看房状态
        HouseSubscribe subscribe = houseSubscribeMapper.getHouseSubscribeByUserAndHouse(house.getId(),1);
        if(null != subscribe){
            result.setSubscribeStatus(subscribe.getStatus());
        }
        return result;
    }

    @Override
    public Object getCityAndRegion(String cityName, String regionName) {
        Map<String,Object> resultMap = new HashMap<>();
        SupportAddressExample supportAddressExample = new SupportAddressExample();
        supportAddressExample.createCriteria().andEnNameEqualTo(cityName).andLevelEqualTo("city");
        SupportAddress city = supportAddressMapper.selectByExample(supportAddressExample).get(0);

        supportAddressExample.clear();
        supportAddressExample.createCriteria().andEnNameEqualTo(regionName).andBelongToEqualTo(city.getEnName());
        SupportAddress region = supportAddressMapper.selectByExample(supportAddressExample).get(0);

        resultMap.put("city", city);
        resultMap.put("region", region);
        return resultMap;
    }

    @Override
    public Object changeHouseStatus(Long id, int status) {
        HouseExample houseExample = new HouseExample();
        houseExample.createCriteria().andIdEqualTo(id.intValue());
        List<House> houses = houseMapper.selectByExample(houseExample);
        if (houses.size() < 1){
            throw new BusinessException("未找到对应房屋信息");
        }
        if (houses.get(0).getStatus() == status) {
            throw new BusinessException("状态没有发生变化");
        }

        if (houses.get(0).getStatus() == HouseStatus.RENTED.getValue()) {
            throw new BusinessException("已出租的房源不允许修改状态");
        }

        if (houses.get(0).getStatus() == HouseStatus.DELETED.getValue()) {
            throw new BusinessException("已删除的资源不允许操作");
        }

        houseExtMapper.updateHouseStatus(id, status);

        //此处用rabbitmq吧id发送到broker后, 异步处理
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("status",status);
        paramMap.put("houseId",id);
        mqSender.send(MqConstants.ES_TOPIC_ROUTING_KEY, JsonMapper.toJsonString(paramMap));

        return new ResultHelper().newSuccessResult();
    }

    /**
     * 房源详细信息对象填充
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private void wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        SubwayExample subwayExample = new SubwayExample();
        subwayExample.createCriteria().andIdEqualTo(houseForm.getSubwayLineId().intValue());
        List<Subway> subwayList = subwayMapper.selectByExample(subwayExample);
        if (subwayList.size() == 0) {
            throw new BusinessException("Not valid Subway");
        }

        SubwayStationExample subwayStationExample = new SubwayStationExample();
        subwayStationExample.createCriteria().andIdEqualTo(houseForm.getSubwayStationId().intValue());
        List<SubwayStation> subwayStationList = subwayStationMapper.selectByExample(subwayStationExample);
        if (subwayStationList.size() == 0 || subwayList.get(0).getId() != subwayStationList.get(0).getSubwayId()) {
            throw new BusinessException("Not valid subway station!");
        }

        houseDetail.setSubwayLineId(subwayList.get(0).getId());
        houseDetail.setSubwayLineName(subwayList.get(0).getName());

        houseDetail.setSubwayStationId(subwayStationList.get(0).getId());
        houseDetail.setSubwayStationName(subwayStationList.get(0).getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
    }

    /**
     * 图片对象列表信息填充
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, int houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(picUrl);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    private Object getHouseListByEs(List<Integer> houseIds){
        PageInfo pageInfo = new PageInfo();
        List<HouseDTO> houseDTOS = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();
        //用来矫正顺序的
        Map<Integer, HouseDTO> idToHouseMap = new HashMap<>();

        HouseExample houseExample = new HouseExample();
        houseExample.createCriteria().andIdIn(houseIds);
        List<House> houseList = houseMapper.selectByExample(houseExample);
        pageInfo.setTotal(houseList.size());

        houseList.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house,HouseDTO.class);
            houseDTO.setCover(picUrl + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        // 根据houseId的list获取house detail列表
        HouseDetailExample houseDetailExample = new HouseDetailExample();
        houseDetailExample.createCriteria().andHouseIdIn(houseIds);
        List<HouseDetail> details = houseDetailMapper.selectByExample(houseDetailExample);

        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        HouseTagExample houseTagExample = new HouseTagExample();
        houseTagExample.createCriteria().andHouseIdIn(houseIds);
        List<HouseTag> houseTags = houseTagMapper.selectByExample(houseTagExample);

        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });

        // 矫正顺序
        if (houseIds.size() > 0 ){
            for (Integer houseId : houseIds) {
                houseDTOS.add(idToHouseMap.get(houseId));
            }
        }

        pageInfo.setList(houseDTOS);
        return pageInfo;
    }

    private Object getHouseListsNormal(RentSearch rentSearch){
        int page = rentSearch.getStart() / rentSearch.getSize();

        PageHelper.startPage(page + 1,rentSearch.getSize());

        //映射筛选条件
        rentSearch.setOrderBy(new TransferOrderUtil().getDbOrderBy(rentSearch.getOrderBy()));
        List<House> houseList = houseExtMapper.getIndexHouseList(rentSearch);

        PageInfo housePageInfo = new PageInfo(houseList);
        if (houseList.size() == 0 ){
            return housePageInfo;
        }
        houseList = housePageInfo.getList();

        List<HouseDTO> houseDTOS = new ArrayList<>();

        List<Integer> houseIds = new ArrayList<>();
        Map<Integer, HouseDTO> idToHouseMap = Maps.newHashMap();

        ModelMapper modelMapper = new ModelMapper();
        houseList.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.picUrl + house.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        // 根据houseId的list获取house detail列表
        HouseDetailExample houseDetailExample = new HouseDetailExample();
        houseDetailExample.createCriteria().andHouseIdIn(houseIds);
        List<HouseDetail> details = houseDetailMapper.selectByExample(houseDetailExample);

        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        HouseTagExample houseTagExample = new HouseTagExample();
        houseTagExample.createCriteria().andHouseIdIn(houseIds);
        List<HouseTag> houseTags = houseTagMapper.selectByExample(houseTagExample);

        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
        housePageInfo.setList(houseDTOS);
        return housePageInfo;
    }
}
