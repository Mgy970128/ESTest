package top.mgy.service;


import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.mgy.pojo.Content;
import top.mgy.utils.HtmlParseUtil;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
//业务编写
public class ContextService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    /**
     * 解析数据放入es索引库中
     */
    public Boolean pareseContext(String keyword) throws IOException {
        List<Content> list = HtmlParseUtil.HtmlParse(keyword);
        //把查询数据插入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");

        list.stream().forEach(item->
            bulkRequest.add(new IndexRequest("jd_goods").id(UUID.randomUUID().toString().replace("-","").substring(8))
                    .source(JSON.toJSONString(item),XContentType.JSON))
        );

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //判断是否失败
        return !bulk.hasFailures();
    }


    /**
     * ES 中根据关键字匹配查询数据,并高亮显示关键字
     * @param keyword
     * @param pageNo
     * @param pageSize
     * @return
     * @throws IOException
     */
    public List<Map<String,Object>> result(String keyword,int pageNo,int pageSize) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }
        //条件搜索
        SearchRequest searchResult = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);
        //精准匹配查询构建  只要 title包含关键字
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
        sourceBuilder.query(matchQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60,TimeUnit.SECONDS));

        //高亮配置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮字段
        highlightBuilder.field("title");
        //关闭多个高亮显示
        highlightBuilder.requireFieldMatch(false);
        //前缀
        highlightBuilder.preTags("<span style='color:red'>");
        //后缀
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        searchResult.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchResult, RequestOptions.DEFAULT);
        //解析返回结果
        List<Map<String,Object>> list = new ArrayList<>();
        //封装返回结果
        Arrays.stream(search.getHits().getHits()).forEach(item->{
            //将原结果中的title字段替换为高亮字段
            //step1 :获取高亮字段
            Map<String, HighlightField> highlightFields = item.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            //step2:获取原来结果
            Map<String, Object> sourceAsMap = item.getSourceAsMap();
            //step3:替换
            if(title != null){
                Text[] fragments = title.fragments();
                String  newTitle="";
                for (Text text : fragments) {
                    newTitle += text;
                }
                sourceAsMap.put("title",newTitle);
            }

            list.add(sourceAsMap);
        });
        return list;
    }
}
