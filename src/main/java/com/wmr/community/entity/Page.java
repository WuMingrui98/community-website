package com.wmr.community.entity;

/**
 * 用来封装分页相关的信息的
 */
public class Page {
    // 当前页码
    private int current = 1;
    // 显示上限
    private int limit = 10;
    // 数据总数(用来计算总分页数)
    private int rows;
    // 查询路径(用来复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current > 0) this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获得当前页的起始行
     *
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotal() {
        return rows / limit + (rows % limit == 0 ? 0 : 1);
    }

    /**
     * 获得分页的起始页
     *
     * @return
     */
    public int getFrom() {
        if (current + 2 > getTotal()) {
            return Math.max(getTotal() - 4, 1);
        }
        return Math.max(current - 2, 1);
    }

    /**
     * 获得分页的结束页
     *
     * @return
     */
    public int getTo() {
        int from = getFrom();
        return Math.min(from + 4, getTotal());
    }
}
