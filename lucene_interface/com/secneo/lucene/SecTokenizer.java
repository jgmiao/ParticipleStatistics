package com.secneo.lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.split.SplitWordTool2;


public class SecTokenizer extends Tokenizer {
    // 下面三个attr的说明参考 http://blog.csdn.net/mr_tank_/article/details/11114581
    /** 当前词 */
    private final CharTermAttribute termAtt;
    /** 偏移量 */
    private final OffsetAttribute offsetAtt;
    /** 距离 */
    private final PositionIncrementAttribute positionAttr;

    private final Reader input;

    private final List<Tuple> resultList;
    private int resultIndex;

    // private final String apkName;

    public SecTokenizer(Reader input) throws IOException {
        super(input);
        this.termAtt = this.addAttribute(CharTermAttribute.class);
        this.offsetAtt = this.addAttribute(OffsetAttribute.class);
        this.positionAttr = this.addAttribute(PositionIncrementAttribute.class);
        this.input = input;
        this.resultList = this.parseToWordList(readerToString(this.input));
        this.resultIndex = 0;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (this.resultList == null || (this.resultList.size() < this.resultIndex + 1)) {
            return false;
        }

        this.clearAttributes();
        Tuple t = this.resultList.get(this.resultIndex);
        this.termAtt.setEmpty().append(t.word);
        this.offsetAtt.setOffset(t.offsetBegin, t.offsetEnd);
        this.positionAttr.setPositionIncrement(t.position);

        this.resultIndex++;
        return true;
    }

    private static class Tuple implements Comparable<Tuple> {
        public Tuple(String word, int offsetBegin, int offsetEnd) {
            this.word = word;
            this.offsetBegin = offsetBegin;
            this.offsetEnd = offsetEnd;
        }

        public final String word;
        public final int offsetBegin;
        public final int offsetEnd;
        public int position = 1;

        /**
         * 起始位置靠前的，先出现，起始位置相同的，长串先出现
         */
        @Override
        public int compareTo(Tuple o) {
            if (this.offsetBegin == o.offsetBegin) {
                return o.offsetBegin - this.offsetEnd;
            } else {
                return this.offsetBegin - o.offsetBegin;
            }
        }
    }

    private List<Tuple> parseToWordList(String apkName) {
        HashSet<Cell> wordList = SplitWordTool2.splitWordPort(apkName);
        List<Tuple> tupleList = new ArrayList<Tuple>(wordList.size());
        // 填入offset
        for (Cell cell : wordList) {
            String word = cell.getName();
            int offsetBegin = apkName.indexOf(word);
            if (offsetBegin == -1) {
                continue;
            }
            tupleList.add(new Tuple(word, offsetBegin, offsetBegin + word.length()));
        }
        // 排序，填入position
        Collections.sort(tupleList);

        return tupleList;
    }

    private String readerToString(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

}
