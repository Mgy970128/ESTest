package top.mgy.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import top.mgy.pojo.Content;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 爬取京东数搜索据工具类
 */
public class HtmlParseUtil {

    public static List<Content> HtmlParse(String keyword) throws IOException {
        String url = "https://search.jd.com/Search?keyword="+URLEncoder.encode(keyword, "GBK");
        //解析网页
        Document parse = Jsoup.parse(new URL(url),30000);
        Element j_goodsList = parse.getElementById("J_goodsList");
        Elements li = j_goodsList.getElementsByTag("li");

        List<Content> list = new ArrayList<>();
        for (Element element : li) {
            //这里图片是懒加载的，需要通过 source-data-lazy-img 取值
            String image = element.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String price = element.getElementsByClass("p-price").eq(0).text();
            String name = element.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setImg(image);
            content.setPrice(price);
            content.setTitle(name);
            list.add(content);
        }
        return list;
    }
}
