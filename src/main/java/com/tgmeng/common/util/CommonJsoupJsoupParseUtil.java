package com.tgmeng.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgmeng.common.enums.exception.ServerExceptionEnum;
import com.tgmeng.common.exception.ServerException;
import com.tgmeng.model.dto.topsearch.TopSearchMaoYanDTO;
import com.tgmeng.model.vo.topsearch.TopSearchCommonVO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CommonJsoupJsoupParseUtil {
    public static List<TopSearchCommonVO.DataInfo> tengXunShiPin(String content) {
        Document parse = Jsoup.parse(content);
        List<TopSearchCommonVO.DataInfo> topSearchCommonVOS = new ArrayList<>();
        Elements elements = parse.select(".table_list > li");
        for (Element element : elements.subList(1, elements.size())) {
            String url = safeAttr(element," div:nth-of-type(1) > a","href") ;
            String hotScore = "";
            Element spanElement = element.selectFirst("div:nth-of-type(2) span");
            if (spanElement != null) {
                String style = spanElement.attr("style");
                if (style != null && style.contains("width")) {
                    String[] stylePairs = style.split(";");
                    for (String pair : stylePairs) {
                        if (pair.trim().startsWith("width")) {
                            String widthValue = pair.split(":", 2)[1].trim().replace("%", ""); // 例如 "36.41441428782205"
                            try {
                                // 转换为 double
                                double widthDouble = Double.parseDouble(widthValue);
                                // 格式化保留 6 位（包括小数点）
                                String formattedWidth = String.format("%.4f", widthDouble); // 例如 "36.4144"
                                // 乘以 10000，转换为 long
                                Long hotScoreTemp = Math.round(Double.parseDouble(formattedWidth) * 10000); // 例如 364144
                                hotScore = hotScoreTemp.toString(); // 保留字符串形式用于 width 参数
                            } catch (NumberFormatException e) {
                                System.err.println("解析 width 失败: " + widthValue + ", 错误: " + e.getMessage());
                            }
                            break;
                        }
                    }
                }
            }
            String title = safeText(element, "div:nth-of-type(1) > a ");
            topSearchCommonVOS.add(new TopSearchCommonVO.DataInfo(title, StringUtil.stringParseToLong(hotScore), url, "","","","",""));
        }
        return topSearchCommonVOS;
    }

    public static List<TopSearchCommonVO.DataInfo> maoYan(String content) {
        try {
            Document parse = Jsoup.parse(content);
            List<TopSearchCommonVO.DataInfo> topSearchCommonVOS = new ArrayList<>();
            // 2. 获取所有 <script> 标签
            Elements scripts = parse.select("script");
            // 3. 匹配 var AppData = { ... };
            Pattern pattern = Pattern.compile("var\\s+AppData\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
            for (var script : scripts) {
                String content1 = script.html();
                Matcher matcher = pattern.matcher(content1);
                if (matcher.find()) {
                    String appDataJson = matcher.group(1);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    TopSearchMaoYanDTO topSearchMaoYanDTO = mapper.readValue(appDataJson, TopSearchMaoYanDTO.class);
                    for (TopSearchMaoYanDTO.Movies movie : topSearchMaoYanDTO.getData().getData().getMovies()) {
                        topSearchCommonVOS.add(new TopSearchCommonVO.DataInfo(movie.getNm(),
                                movie.getLabel().getNumber()+movie.getLabel().getText(),
                                "https://www.maoyan.com/films/"+movie.getId(),
                                movie.getBackGroundImg(),
                                movie.getStar(),
                                movie.getShortDec(),
                                movie.getCat(),
                                movie.getPubDesc()));
                    }
                    break;
                }
            }
            return topSearchCommonVOS;
        }catch (Exception e){
            log.error("👺👺👺获取猫眼榜单失败👺👺👺", e);
            throw new ServerException(ServerExceptionEnum.MAO_YAN_SEARCH_EXCEPTION);
        }
    }


    public static String safeText(Element parent, String selector) {
        Element el = parent.selectFirst(selector);
        return el != null ? el.text() : "";
    }

    public static String safeAttr(Element parent, String selector, String attr) {
        Element el = parent.selectFirst(selector);
        return el != null ? el.attr(attr) : "";
    }
}
