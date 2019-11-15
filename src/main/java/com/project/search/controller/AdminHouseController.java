package com.project.search.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.project.search.common.exception.BusinessException;
import com.project.search.common.utils.FastDFSClient;
import com.project.search.common.utils.ResultHelper;
import com.project.search.common.utils.SftpUtil;
import com.project.search.common.utils.UuidUtils;
import com.project.search.common.validate.BeanValidators;
import com.project.search.config.interf.PassPermission;
import com.project.search.dao.model.House;
import com.project.search.entity.dto.HouseDTO;
import com.project.search.entity.param.DataTableSearch;
import com.project.search.entity.param.HouseForm;
import com.project.search.service.HouseService;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.project.search.common.utils.DateFormatUtil.DAY_FORMAT;
import static com.project.search.common.utils.DateFormatUtil.dateToString;

@Controller
@Slf4j
@PassPermission(value = "pass")
public class AdminHouseController {
    @Value("${linux.sftp.ip}")
    private String sftpIp;

    @Value("${linux.sftp.port}")
    private int port;

    @Value("${linux.sftp.sftpuser}")
    private String sftpUser;

    @Value("${linux.sftp.pwd}")
    private String sftpPwd;

    @Value("${linux.sftp.remotePath}")
    private String remotePath;

    @Value("${fastdfs.url}")
    private String dfsUrl;

    @Value("${fastdfs.url}")
    private String picUrl;

    @Autowired
    private SftpUtil sftpUtil;

    @Autowired
    private HouseService houseService;

    /**
     * 房源列表页
     * @return
     */
    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }


    /**
     * 新增房源功能页
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }


    /**
     * 房屋列表展示
     * @param searchBody
     * @return
     */
    @PostMapping("admin/houses")
    @PassPermission(value = "")
    @ResponseBody
    public Object houses(@ModelAttribute DataTableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();

        PageInfo pageInfo = (PageInfo)houseService.getHouseLists(searchBody);
        List<House> houses = pageInfo.getList();

        ModelMapper modelMapper = new ModelMapper();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(picUrl + house.getCover());
            houseDTOS.add(houseDTO);
        });

        Map<String,Object> result = new HashMap<>();
        result.put("code","000000");
        result.put("draw",searchBody.getDraw());
        result.put("data",houseDTOS);
        result.put("message","success");
        result.put("recordsTotal",pageInfo.getTotal());
        result.put("recordsFiltered",pageInfo.getTotal());
        return result;
    }


    /**
     * 上传图片接口
     * @param file
     * @return
     */
    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Object uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResultHelper().newSuccessResult("图片为空！");
        }

        //获取文件基本属性
        String fileOriginalName = file.getOriginalFilename();
        String fileName = fileOriginalName.substring(0,fileOriginalName.lastIndexOf("."));
        String fileType = fileOriginalName.substring(fileOriginalName.lastIndexOf(".")+1).toLowerCase();
        Long fileSize = file.getSize();

        //过滤非法文件
        List typeList = Arrays.asList("jpg","png","gif");
        if (!typeList.contains(fileType)){
            throw new BusinessException("非法文件格式");
        }
        if (fileSize > Long.valueOf(1024 * 1024)){
            throw new BusinessException("文件过大");
        }

        //上传文件到sftp
//        try {
//
//            sftpUtil.connectServer(sftpIp,port,sftpUser,sftpPwd);
//            sftpUtil.uploadFile(remotePath + UuidUtils.getUuid()+"." + fileType,
//                    file.getInputStream());
//        } catch (Exception e) {
//            throw new BusinessException("图片存储失败：" + e.getMessage());
//        }

        //上传到fastdfs
        String picPath = null;
        try{
            String propertyPath = System.getProperty("user.dir") + "/src/main/resources/fastDFS.properties";
            FastDFSClient fastDFSClient = new FastDFSClient(propertyPath);

            picPath = fastDFSClient.uploadFile(file.getBytes(),fileType);
//            picPath = dfsUrl + path;
            log.info("图片path:" + picPath);
        } catch (Exception e){
            throw new BusinessException("图片存储失败：" + e.getMessage());
        }
        return new ResultHelper().newSuccessResult(picPath);
    }

    /**
     * 新增房源接口
     * @param houseForm
     * @return
     */
    @PostMapping("admin/add/house")
    @ResponseBody
    public Object addHouse(@ModelAttribute("form-house-add") HouseForm houseForm) {
        //校验参数
        BeanValidators.validate(houseForm);

        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            throw new BusinessException("必须上传图片");
        }

        return houseService.addHouseInfo(houseForm);
    }

    /**
     * 审核接口
     * @param id
     * @param status
     * @return
     */
    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public Object operateHouse(@PathVariable(value = "id") Long id,
                                    @PathVariable(value = "operation") int status) {
        if (id <= 0) {
            throw new BusinessException("房屋id错误");
        }

        return houseService.changeHouseStatus(id,status);
    }
}
