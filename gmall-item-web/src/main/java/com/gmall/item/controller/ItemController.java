package com.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.gmall.bean.PmsProductSaleAttr;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.bean.PmsSkuSaleAttrValue;
import com.gmall.service.SkuService;
import com.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap modelMap){
        // sku对象
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        modelMap.put("skuInfo", pmsSkuInfo);

        // 销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrs =
                spuService.spuSaleAttrListCheckBySku(
                        pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        // 查询当前sku的spu的其他sku的hash表集合
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(
                pmsSkuInfo.getProductId());
        Map<String, String> skuSaleAttrHash = new HashMap<>();
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String v = skuInfo.getId();
            StringBuilder k = new StringBuilder();
            List<PmsSkuSaleAttrValue> skuAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (int i = 0; i < skuAttrValueList.size(); i ++){
                if (i > 0){
                    k.append("|");
                }
                k.append(skuAttrValueList.get(i).getSaleAttrValueId());
            }
            skuSaleAttrHash.put(k.toString(), v);
        }
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);

        return "item";
    }

}
