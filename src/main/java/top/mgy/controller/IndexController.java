package top.mgy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import top.mgy.service.ContextService;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {


    @Autowired
    private ContextService contextService;

    /**
     * 搜索页面映射
     * @return
     */
    @GetMapping({"/","/index"})
    public String index(){
        return "index";
    }


    /**
     * 从京东爬取关键字商品，并放入es的jd_goods索引库中
     * @param keyword
     * @return
     * @throws IOException
     */
    @GetMapping("/parse/{keyword}")
    @ResponseBody
    public Boolean parse(@PathVariable("keyword") String keyword) throws IOException {
        System.out.println(keyword);
        return  contextService.pareseContext(keyword);
    }

    /**
     * 前端查询数据接口
     * @param keyword  关键字
     * @param pageNo    页码
     * @param pageSize  每页个数
     * @return
     * @throws IOException
     */
    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    @ResponseBody
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,
                                           @PathVariable("pageNo") int pageNo,
                                           @PathVariable("pageSize") int pageSize) throws IOException {

        if(pageNo == 0){
            pageNo = 1;
        }
        System.out.println(keyword);
        List<Map<String, Object>> result = contextService.result(keyword, pageNo, pageSize);
        return result;

    }
}
