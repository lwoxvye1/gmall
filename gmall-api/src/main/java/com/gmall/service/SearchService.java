package com.gmall.service;

import com.gmall.bean.PmsSearchParam;
import com.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
