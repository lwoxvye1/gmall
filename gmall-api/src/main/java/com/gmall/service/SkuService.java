package com.gmall.service;

import com.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();

    boolean checkPrice(String productSkuId, BigDecimal productPrice);
}
