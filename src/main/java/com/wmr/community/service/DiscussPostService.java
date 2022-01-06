package com.wmr.community.service;

import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    private DiscussPostMapper discussPostMapper;

    private SensitiveFilter sensitiveFilter;

    @Autowired
    public void setDiscussPostMapper(DiscussPostMapper discussPostMapper) {
        this.discussPostMapper = discussPostMapper;
    }

    @Autowired
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }

    /**
     * 根据分页要求按用户id查询数据库中的帖子（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @param offset mysql的offset
     * @param limit mysql的limit
     * @return 返回根据分页要求按用户id查询数据库中的帖子
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 查询userId对应的用户的发帖数（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @return 返回userId对应的用户的发帖数
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 将discussPost处理后，传递给持久层处理
     * @param discussPost 初步封装的discussPost
     * @return 返回持久层的处理结果
     */
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转移HTML标签
        String title = HtmlUtils.htmlEscape(discussPost.getTitle());
        String content = HtmlUtils.htmlEscape(discussPost.getContent());
        // 过滤敏感词
        title = sensitiveFilter.filter(title);
        content = sensitiveFilter.filter(content);

        discussPost.setContent(content);
        discussPost.setTitle(title);

        return discussPostMapper.insertDiscussPost(discussPost);
    }
}
