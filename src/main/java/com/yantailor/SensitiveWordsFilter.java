package com.yantailor;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yantailor
 * on 2022/9/10 11:02 @Version 1.0
 */
public class SensitiveWordsFilter {


    public static void main(String[] args) throws IOException {
        SensitiveWordsFilter sensitiveWordsFilter = new SensitiveWordsFilter();
        System.out.println(sensitiveWordsFilter.filter("很黄*色&*很暴*&*力"));
    }

    //替换符号
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    public SensitiveWordsFilter() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("SensitiveWords.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        String keyword;
        while((keyword = bufferedReader.readLine()) != null){
            String s =keyword;
            this.addKeyWord(s);
        }
    }
    //将敏感词添加到前缀树
    private void addKeyWord(String keyword){
        TrieNode tempNode = rootNode;

        for(int i = 0 ; i < keyword.length() ; i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeyWordEnd(true);
            }

        }

    }

    /*
    * 过滤敏感词
    * */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder stringBuilder = new StringBuilder();

        while(position < text.length()){
            char c = text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if(tempNode == rootNode){
                    stringBuilder.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin开头的字符串不是敏感词
                stringBuilder.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //重新指向根节点
                tempNode = rootNode;
            }else if (tempNode.isKeyWordEnd()){
                //发现敏感词，将begin-position字符串替换
                stringBuilder.append(REPLACEMENT);
                //进入下一个位置
                begin = ++position;
                //重新指向根节点
                tempNode = rootNode;

            }else {
                //检查下一个字符
                position++;
            }
        }
        stringBuilder.append(text.substring(begin));

        return stringBuilder.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //0x2E80 -- 0x9FFF东亚字符范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    private class TrieNode{
        //关键词结束标识
        private boolean isKeyWordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        public void addSubNode(Character c , TrieNode trieNode){
            subNodes.put(c, trieNode);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
