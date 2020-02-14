package com.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.PmsProductImage;
import com.gmall.bean.PmsProductInfo;
import com.gmall.bean.PmsProductSaleAttr;
import com.gmall.service.SpuService;
import com.gmall.manager.util.PmsUploadUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @RequestMapping("/spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){
        return spuService.spuList(catalog3Id);
    }

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        // 将图片或者音视频上传到分布式的文件存储系统
        // 将图片的元数据返回给页面
        return PmsUploadUtil.uploadImage(multipartFile);
    }

    @RequestMapping("/saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    @RequestMapping("/spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        return spuService.spuSaleAttrList(spuId);
    }

    @RequestMapping("/spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){
        return spuService.spuImageList(spuId);
    }
}
