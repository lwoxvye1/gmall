package com.gmall.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.gmall.bean.PmsSearchParam;
import com.gmall.bean.PmsSearchSkuInfo;
import com.gmall.bean.PmsSkuAttrValue;
import com.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam) {
        String dslStr = getSearchDsl(pmsSearchParam);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder(dslStr)
                .addIndex("gmall").addType("_doc").build();
        SearchResult result = null;

        try {
            result = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits =
                result.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;

            Map<String, List<String>> highlight = hit.highlight;
            if (highlight != null) {
                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);
        }

        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParam pmsSearchParam){
        String [] skuAttrValueList = pmsSearchParam.getValueId();
        String keyword = pmsSearchParam.getKeyword();
        String catalog3Id = pmsSearchParam.getCatalog3Id();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder(
                    "catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if (skuAttrValueList != null && skuAttrValueList.length != 0) {
            for (String  pmsSkuAttrValue: skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder(
                        "skuAttrValueList.valueId", pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(
                    "skuName", keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(20);

        // 排序
        searchSourceBuilder.sort("id", SortOrder.DESC);

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color: red;'>");
        highlightBuilder.postTags("</span>");
        highlightBuilder.field("skuName");
        searchSourceBuilder.highlighter(highlightBuilder);

        return searchSourceBuilder.toString();
    }
}
