package org.k3scc.jabletvrepo.system;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private VideoMapper videoMapper;

    // 返回视图页面
    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/add")
    @ResponseBody
    public String add(String value) {
        value = value.toUpperCase().trim();
        LambdaQueryWrapper<VideoInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoInfoEntity::getCode, value);
        List<VideoInfoEntity> videoInfoEntities = videoMapper.selectList(wrapper);

        if (videoInfoEntities.size() > 0) {
            if (videoInfoEntities.size() > 1) {
                int i = videoMapper.deleteById(videoInfoEntities.get(1).getId());
            }
            VideoInfoEntity videoInfoEntity = videoInfoEntities.get(0);
            if (videoInfoEntity != null && videoInfoEntity.getStatus().equals("1")) {
                return "已存在";
            }
        }

        String videoUrl = StrUtil.format("https://jable.tv/videos/{}/", value);
        HttpResponse execute = HttpRequest.get(videoUrl)
                .setHttpProxy("127.0.0.1", 10809)
                .timeout(20000)
                .execute();
        String html = execute.body();
        // 解析HTML
        Document doc = Jsoup.parse(html);
        Element headerLeft = doc.selectFirst(".header-left"); // 获取class为header-left的元素
        if (headerLeft != null) {
            Element h4 = headerLeft.selectFirst("h4"); // 获取h4标签
            String h4Content = h4 != null ? h4.text() : "h4标签不存在";
            Element player = doc.selectFirst("#player"); // 获取id为player的元素
            // 获取poster参数
            String poster = player != null ? player.attr("poster") : "id为player的元素不存在或没有poster参数";
            byte[] bytes = HttpRequest.get(poster)
                    .setHttpProxy("127.0.0.1", 10809)
                    .timeout(10000)//超时，毫秒
                    .execute().bodyBytes();
            // 获取响应结果

            // 将图片数据转换为Base64编码
            String base64Image = Base64.encode(bytes);
            if (videoInfoEntities.size() == 0) {
                int insert = videoMapper.insert(VideoInfoEntity.builder()
                        .code(value)
                        .image(base64Image)
                        .videoUrl(videoUrl)
                        .title(h4Content)
                        .imageUrl(poster)
                        .status("1")
                        .createTime(DateUtil.date())
                        .build());
            } else {
                int i = videoMapper.updateById(VideoInfoEntity.builder()
                        .id(videoInfoEntities.get(0).getId())
                        .code(value)
                        .image(base64Image)
                        .videoUrl(videoUrl)
                        .title(h4Content)
                        .imageUrl(poster)
                        .status("1")
                        .createTime(DateUtil.date())
                        .build());
            }
            return "success";
        } else {
            int insert = videoMapper.insert(VideoInfoEntity.builder()
                    .code(value)
                    .videoUrl(videoUrl)
                    .status("2")
                    .createTime(DateUtil.date())
                    .build());
            return "error";
        }
    }

    @PostMapping("/list")
    @ResponseBody
    public String list() {
        QueryWrapper<VideoInfoEntity> queryWrapper = new QueryWrapper<>();
        List<VideoInfoEntity> videoInfoEntities = videoMapper.selectList(queryWrapper);
        return JSONUtil.toJsonStr(videoInfoEntities);
    }

    @GetMapping("/sync")
    @ResponseBody
    public String sync() {
        FileReader fileReader = new FileReader("C:\\Users\\kkexc\\Desktop\\demo.txt");
        List<String> stringList = fileReader.readLines();
        for (String s : stringList) {
            String add = add(s.trim());
            if (!add.equals("success")) {
                Console.log("错误：" + s);
            }
        }
        return "success";
    }
}
