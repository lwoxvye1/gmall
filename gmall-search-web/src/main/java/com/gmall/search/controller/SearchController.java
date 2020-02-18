package com.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.*;
import com.gmall.service.AttrService;
import com.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@CrossOrigin
public class SearchController {
    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    @RequestMapping("/list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);

        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            // crumb
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));

                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            // 查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }

        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);

        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }

        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String... delValueId) {
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String[] skuAttrValueList = pmsSearchParam.getValueId();

        StringBuilder urlParam = new StringBuilder();

        if (StringUtils.isNotBlank(keyword)) {
            urlParam.append("&keyword=");
            urlParam.append(keyword);
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            urlParam.append("&catalog3Id=");
            urlParam.append(catalog3Id);
        }

        if (skuAttrValueList != null && skuAttrValueList.length != 0) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (delValueId != null && delValueId.length != 0) {
                    if (!pmsSkuAttrValue.equals(delValueId[0])) {
                        urlParam.append("&valueId=");
                        urlParam.append(pmsSkuAttrValue);
                    }
                } else {
                    urlParam.append("&valueId=");
                    urlParam.append(pmsSkuAttrValue);
                }
            }
        }
        urlParam.deleteCharAt(0);
        return urlParam.toString();
    }
}
