package com.tgmeng.model.dto.topsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * description: 抖音热搜官方返回的VO
 * package: com.tgmeng.model.vo.topsearch
 * className: TopSearchDouYinDTO
 *
 * @author tgmeng
 * @version v1.0
 * @since 2025/6/29 22:42
*/
@Data
@Accessors(chain = true)
public class TopSearchDouYinDTO {
    private DataView data;

    @Data
    public static  class DataView {
        /** 热榜列表 */
        @JsonProperty("word_list")
        private List<DataVO> wordList;
    }

    @Data
    public static  class DataVO {
        /** 热搜词 */
        private String word;
        /** 这个是用来拼url的   https://www.douyin.com/hot/{{sentence_id}}/{{word}}*/
        @JsonProperty("sentence_id")
        private String sentenceId;
        /** 热搜词的热度 */
        @JsonProperty("hot_value")
        private Long hotValue;
    }
}