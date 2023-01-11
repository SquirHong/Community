package com.hjs.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hong
 * @create 2023-01-07 15:52
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "* * *";

    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init(){

        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        ){
            String keyword;
            while ((keyword = br.readLine()) != null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词树失败"+e.getMessage());
        }
    }

    private void addKeyword(String keyword) {
        TrieNode tmp = root;
        for (int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tmp.getSubNode(c);
            if (subNode == null){
                subNode = new TrieNode();
                tmp.addSubNode(c,subNode);
            }
            tmp = subNode;
            if (i == keyword.length()-1){
                tmp.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词的方法
     * @param text 要被过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        TrieNode tmp = root;
        int begin = 0;
        int end = 0;
        while (begin < text.length()){
            char c = text.charAt(end);
            //跳过字符
            if (issymbol(c)){
                //还未进入前缀树
                if (tmp == root){
                    sb.append(c);
                    begin++;
                }
                end++;
                continue;
            }
            //普通字符
            tmp = tmp.getSubNode(c);
            // roo节点下不含有 c 字符的节点
            if (tmp == null){
                sb.append(text.charAt(begin));
                end = ++begin;
                tmp = root;
            }else if (tmp.isKeyWordEnd){
                //已经到了前缀树的叶子节点
                sb.append(REPLACEMENT);
                begin = ++end;
                tmp = root;
            }else {
                //在前缀树的一条路径的中间节点中
                if (end < text.length()-1){
                    end++;
                }
            }
        }
//        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     *
     * @param c
     * @return 返回true，则表明是特殊字符，   返回false，则表明是普通字符
     */
    private boolean issymbol(char c) {
//        0x2E80 ~ 0x9FFF 是东亚文字
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //前缀树
    private class TrieNode{

        //敏感词结束标志
        boolean isKeyWordEnd = false;

        //字节点
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }

}
