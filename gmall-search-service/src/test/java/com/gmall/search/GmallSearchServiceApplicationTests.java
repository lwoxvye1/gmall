package com.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.gmall.bean.PmsSearchSkuInfo;
import com.gmall.bean.PmsSkuInfo;
import com.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GmallSearchServiceApplicationTests {
    @Reference
    private  SkuService skuService;

    @Autowired
    JestClient jestClient;

    /**
     * 将mysql数据转化为es的数据结构导入es
     */
    @Test
    void put() throws IOException {
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList){
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();

            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);

            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }

        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index put = new Index.Builder(pmsSearchSkuInfo)
                    .index("gmall").type("_doc").id(pmsSearchSkuInfo.getId())
                    .build();
            jestClient.execute(put);
        }
    }

    /**
     * 复杂查询
     */
    @Test
    void Search() throws IOException{
        Search search = new Search.Builder("{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"term\": {\n" +
                "            \"skuAttrValueList.valueId\": \"123\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "            \"skuName\": \"联想电脑\"\n" +
                "            }\n" +
                "          \n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}")
                .addIndex("gmall").addType("_doc")
                .build();
        SearchResult result = jestClient.execute(search);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits =
                result.getHits(PmsSearchSkuInfo.class);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);
        }

        System.out.println(pmsSearchSkuInfos);
    }


    @Test
    void Search2() throws IOException {
        // jest的dsl工具
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // filter
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(
                "skuAttrValueList.valueId", "43");
        boolQueryBuilder.filter(termQueryBuilder);

        // must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(
                "skuName", "华为");
        boolQueryBuilder.must(matchQueryBuilder);

        // query
        searchSourceBuilder.query(boolQueryBuilder);

        // from
        searchSourceBuilder.from(0);

        // size
        searchSourceBuilder.size(20);

        // highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        searchSourceBuilder.highlighter(highlightBuilder);

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("gmall").addType("_doc").build();
        SearchResult result = jestClient.execute(search);

        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits =
                result.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            pmsSearchSkuInfos.add(source);
        }

        System.out.println(pmsSearchSkuInfos);
    }
}
