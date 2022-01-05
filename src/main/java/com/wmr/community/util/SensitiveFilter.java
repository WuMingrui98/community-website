package com.wmr.community.util;

import org.apache.commons.lang3.CharUtils;
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

@Component
public class SensitiveFilter {
    private final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private final String REPLACE_WORD = "***";

    // 根节点
    private final TrieNode root = new TrieNode();

    /**
     * SensitiveFilter对象有Spring创建好后，自动将sensitive-words.txt中的敏感词构建成前缀树
     */
    @PostConstruct
    public void init() throws IOException {
        // 1. 读取sensitive-words.txt
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = SensitiveFilter.class.getClassLoader().getResourceAsStream("sensitive-words.txt");
            if (inputStream == null) {
                throw new RuntimeException("sensitive-words.txt文件不存在!");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String sensitiveWord;
            while ((sensitiveWord = bufferedReader.readLine()) != null) {
                addSensitiveWord(sensitiveWord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) inputStream.close();
            if (bufferedReader != null) bufferedReader.close();
        }
    }

    /**
     * 实现将预设的敏感词加入前缀树的功能
     *
     * @param sensitiveWord 敏感词
     */
    public void addSensitiveWord(String sensitiveWord) {
        char[] characters = sensitiveWord.toCharArray();
        TrieNode temp = root;
        int index = 0;
        for (char character : characters) {
            TrieNode childNode = temp.getChildNode(character);
            if (childNode == null) {
                temp.setChildNode(character);
                childNode = temp.getChildNode(character);
            }
            temp = childNode;
            if (index == sensitiveWord.length() - 1) {
                temp.setEndTag(true);
            }
            index++;
        }
    }

    /**
     * 实现过滤敏感词的功能
     *
     * @param text 带过滤的文本
     * @return 返回过滤后的文本
     */
    public String filter(String text) {
        int len = text.length();
        // 指针temp指向前缀树
        TrieNode temp = root;
        // 指针1是字符串的前一个指针
        int index1 = 0;
        // 指针2是字符串的后一个指针
        int index2 = 0;
        // StringBuilder来保存结果
        StringBuilder stringBuilder = new StringBuilder();
        while (index1 < len) {
            // 判断index2是否到字符串尾部,则将index1往前移动一位
            if (index2 == len) {
                stringBuilder.append(text.charAt(index1));
                index1++;
                index2 = index1;
                temp = root;
                continue;
            }
            char c = text.charAt(index2);
            TrieNode childNode = temp.getChildNode(c);
            // 判断是否匹配上了敏感词
            if (childNode != null) {
                // 判断是否为敏感词结尾
                if (childNode.isEndTag()) {
                    stringBuilder.append(REPLACE_WORD);
                    index1 = ++index2;
                    temp = root;
                } else {
                    index2++;
                    while (index2 < len && isSymbol(text.charAt(index2))) {
                        index2++;
                    }
                    temp = childNode;
                }
            } else {
                stringBuilder.append(text, index1, ++index2);
                index1 = index2;
                temp = root;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 判断是否为特殊符号（需要排除掉东亚文字范围）
     *
     * @param c 待判断符号
     * @return 是特殊，则返回true，不是则返回false
     */
    private boolean isSymbol(char c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);

    }

    // 前缀树节点类
    private class TrieNode {
        // 结束标记，表明这个节点是否为一个单词的结尾
        private boolean endTag = false;

        public boolean isEndTag() {
            return endTag;
        }

        public void setEndTag(boolean endTag) {
            this.endTag = endTag;
        }

        // 用哈希表来存储子节点，key是字符，value是TrieNode对象
        Map<Character, TrieNode> map = new HashMap<>();

        /**
         * 为该节点添加代表字符a的子节点
         *
         * @param a 字符a
         */
        public void setChildNode(char a) {
            map.putIfAbsent(a, new TrieNode());
        }

        /**
         * 取出代表字符a的节点
         *
         * @param a 字符a
         * @return 如果节点存在就返回节点，不存在就返回null
         */
        public TrieNode getChildNode(char a) {
            return map.get(a);
        }
    }
}
