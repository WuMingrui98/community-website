package com.wmr.community;

import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.dao.elasticsearch.DiscussPostRepository;
import com.wmr.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Test
    public void testInsert() {
        for (int i = 109; i < 300; i++) {
            DiscussPost discussPost = discussPostMapper.selectDiscussPostById(i);
            if (discussPost != null) discussPostRepository.save(discussPost);
        }
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100, 0));
    }


    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("????????????,????????????.");
        discussPostRepository.save(post);
    }


    @Test
    public void testDelete() {
//         discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }




    @Test
    public void testSearchByTemplate() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC), SortBuilders.fieldSort("score").order(SortOrder.DESC), SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //??????
        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        //???????????????????????????
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        //????????????????????????????????????????????????
        List<DiscussPost> posts = new ArrayList<>();
        //?????????????????????????????????
        for(SearchHit<DiscussPost> searchHit:searchHits){
            //???????????????
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            //???????????????????????????content???
            searchHit.getContent().setTitle(highlightFields.get("title")==null ? searchHit.getContent().getTitle():highlightFields.get("title").get(0));
            searchHit.getContent().setContent(highlightFields.get("content")==null ? searchHit.getContent().getContent():highlightFields.get("content").get(0));
            //??????????????????
            posts.add(searchHit.getContent());
        }
        System.out.println(posts.size());
        System.out.println(search.getTotalHits());
        for (DiscussPost post : posts) {
            System.out.println(post);
        }
    }
}
