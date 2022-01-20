package com.wmr.community.service;

import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.dao.elasticsearch.DiscussPostRepository;
import com.wmr.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    private DiscussPostRepository discussPostRepository;

    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public void setDiscussPostRepository(DiscussPostRepository discussPostRepository) {
        this.discussPostRepository = discussPostRepository;
    }

    @Autowired
    public void setElasticsearchRestTemplate(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * 向Elasticsearch中存入帖子
     * @param discussPost 封装好的帖子
     */
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    /**
     * 从Elasticsearch中删除帖子
     * @param discussPost 封装好的帖子
     */
    public void deleteDiscussPost(DiscussPost discussPost) {
        discussPostRepository.delete(discussPost);
    }


    /**
     * 从Elasticsearch中按关键字搜索帖子
     * 支持分页、关键词高亮
     *
     * @param keyword 关键词
     * @param current 当前页
     * @param limit 当前页展示个数
     * @return 返回查询到的结果
     */
    public SearchHits<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 构建查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 查询的字段
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                // 排序
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC), SortBuilders.fieldSort("score").order(SortOrder.DESC), SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(current, limit))
                // 高亮
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //查询
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        //得到查询返回的内容
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        //遍历返回的内容进行高亮处理
        for(SearchHit<DiscussPost> searchHit:searchHits){
            //高亮的内容
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            //将高亮的内容填充到content中
            searchHit.getContent().setTitle(highlightFields.get("title")==null ? searchHit.getContent().getTitle():highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content")==null ? searchHit.getContent().getContent():highlightFields.get("content").get(0));
            //放到实体类中
        }
        return search;
    }
}
