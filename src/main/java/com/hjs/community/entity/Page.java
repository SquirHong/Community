package com.hjs.community.entity;

import lombok.Data;

/**
 * @author hong
 * @create 2022-12-30 20:17
 */

public class Page {

    private int current=1;

    private int limit=10;

    private int rows;
    //用于复用分页链接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 50){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffset(){
        return (current-1)*limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal(){
        if (rows % limit == 0){
            return rows/limit;
        }else {
            return rows/limit+1;
        }
    }

    //起始页码
    public int getFrom(){
        int from = current - 2;
        return from < 1 ? 1 : from;
    }
    //终止页码
    public int getTo(){
        return Math.min(getTotal(),current+2);
    }
}
