package top.mgy;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import top.mgy.pojo.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    //索引的创建
    public void testCreateIndex() throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("mgy_index");
        //客户端执行请求,返回执行结果
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);

    }

    //获取索引   测试索引是否存在
    @Test
    public void testExistIndex() throws IOException {
        GetIndexRequest indexRequest = new GetIndexRequest("mgy_index");
        //判断索引是否存在
        boolean exists = restHighLevelClient.indices().exists(indexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);

    }

    //删除索引请求
    @Test
    public void testDelIndex(){

        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("mgy_index");
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            //为true表示删除成功
            System.out.println(delete.isAcknowledged());
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("删除索引失败");
        }
    }

    //添加文档
    @Test
    void testAddDocument() throws IOException {
        User user = new User("告一段落", 22);
        //创建请求
        IndexRequest request = new IndexRequest("mgy_index");

        //设置请求规则  PUT /mgy_index/_doc/1
        //设置ID
        request.id("1");
        //设置超时
        request.timeout(TimeValue.timeValueSeconds(5));
        //将数据传为json，放入请求
        request.source(JSON.toJSONString(user),XContentType.JSON);

        //客户端发送请求  获取响应结果
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);

        System.out.println(indexResponse);
        System.out.println(indexResponse.status());


    }

    //判断文档是否存在
    @Test
    void testExistDocument() throws IOException {
        GetRequest request = new GetRequest("mgy_index", "1");


        //不获取返回的上下文 _source
        request.fetchSourceContext(new FetchSourceContext(false));

        boolean exists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);

    }


    //获取文档信息
    @Test
    void testGetDocumentInfo() throws IOException {
        GetRequest request = new GetRequest("jd_goods", "dmD-e3EBraGcAp6cnnA0");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        //打印文档内容
        System.out.println(response.getSourceAsString());
        System.out.println(response); //全部返回信息
    }

    //更新文档信息
    @Test
    void testUpdateDocumentInfo() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("mgy_index", "1");
        User user = new User();
        user.setName("告一段落666");
        updateRequest.doc(JSON.toJSONString(user),XContentType.JSON);

        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(updateResponse.status());
    }


    //删除文档信息
    @Test
    void testDeleteDocumentInfo() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("mgy_index", "1");

        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(deleteResponse.status());
    }

    //批量插入数据
    @Test
    void testBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("10s");
        Logger logger = LoggerFactory.getLogger(getClass());
        List<User> list = Arrays.asList(new User("告一段落1", 21),
                new User("告一段落2", 22),
                new User("告一段落3", 23),
                new User("告一段落4", 24),
                new User("告一段落5", 25),
                new User("告一段落6", 26));
        logger.info("插入6条数据");

        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(new IndexRequest("mgy_index")
                    .id(i+"")
                    .source(JSON.toJSONString(list.get(i)),XContentType.JSON));
        }

        BulkResponse response = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(response.hasFailures()); //是否失败
    }
}
