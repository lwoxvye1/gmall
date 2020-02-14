package com.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.PmsSkuImage;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuController {
    @Reference
    SkuService skuService;

    @RequestMapping("/saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isBlank(skuDefaultImg)){
            List<PmsSkuImage> pmgSkuImageList = pmsSkuInfo.getSkuImageList();
            if (pmgSkuImageList != null && pmgSkuImageList.size() != 0) {
                pmsSkuInfo.setSkuDefaultImg(pmgSkuImageList.get(0).getImgUrl());
            }
        }

        skuService.saveSkuInfo(pmsSkuInfo);
        return "success";
    }
}
