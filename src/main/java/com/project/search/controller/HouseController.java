package com.project.search.controller;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.project.search.common.utils.ResultHelper;
import com.project.search.config.interf.PassPermission;
import com.project.search.dao.model.House;
import com.project.search.dao.model.SupportAddress;
import com.project.search.dao.model.User;
import com.project.search.entity.dto.HouseDTO;
import com.project.search.entity.param.RentSearch;
import com.project.search.entity.param.RentValueBlock;
import com.project.search.service.HouseService;
import com.project.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@PassPermission(value = "pass")
@Slf4j
public class HouseController {
    @Autowired
    private UserService userService;

    @Autowired
    private HouseService houseService;

    /**
     * 获取支持城市列表
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public Object getSupportCities() {
       return new ResultHelper().newSuccessResult(houseService.getSupportAddress());
    }

    /**
     * 获取对应城市支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public Object getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        return new ResultHelper().newSuccessResult(houseService.getSupportRegions(cityEnName));
    }

    /**
     * 获取具体城市所支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public Object getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        return new ResultHelper().newSuccessResult(houseService.getSupportSubwayLine(cityEnName));
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public Object getSupportSubwayStation(@RequestParam(name = "subway_id") int subwayId) {
        return new ResultHelper().newSuccessResult(houseService.getSubwayStation(subwayId));
    }


    /**
     * 根据地区展示房源信息
     * @param rentSearch
     * @param model
     * @param session
     * @param redirectAttributes
     * @return
     */
    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        //判断有没有cityENname
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }


        List<SupportAddress> supportAddressList = (List<SupportAddress>)houseService.getSupportAddress(rentSearch.getCityEnName());
        if (supportAddressList.size() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        model.addAttribute("currentCity", supportAddressList.get(0));

        List<SupportAddress> SupportRegionsList = (List<SupportAddress>)houseService.getSupportRegions(rentSearch.getCityEnName());
        if (SupportRegionsList.size() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }

        //获取房屋的列表
        PageInfo housePageInfo = (PageInfo)houseService.getIndexHouseLists(rentSearch);

        model.addAttribute("total", housePageInfo.getTotal());
        model.addAttribute("houses", housePageInfo.getList());

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("regions", SupportRegionsList);

        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);

        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId,
                       Model model) {
        if (houseId <= 0) {
            return "404";
        }

        HouseDTO houseDTO = (HouseDTO)houseService.getHouseAllInfo(houseId);
        if (null == houseDTO) {
            return "404";
        }

        Map<String,Object> addressMap = (Map)houseService.getCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());
        SupportAddress city = (SupportAddress)addressMap.get("city");
        SupportAddress region = (SupportAddress)addressMap.get("region");

        model.addAttribute("city", city);
        model.addAttribute("region", region);

        User user = userService.getById(houseDTO.getAdminId());
        model.addAttribute("agent", user);
        model.addAttribute("house", houseDTO);

//        ServiceResult<Long> aggResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(), houseDTO.getDistrict());
//        model.addAttribute("houseCountInDistrict", aggResult.getResult());

        return "house-detail";
    }
}
