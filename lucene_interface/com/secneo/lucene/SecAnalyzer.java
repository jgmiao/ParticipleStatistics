package com.secneo.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public class SecAnalyzer extends Analyzer{

    @Override
    protected TokenStreamComponents createComponents(String fieldName, final Reader reader) {
        Tokenizer tokenizer = null;
        try {
            tokenizer = new SecTokenizer(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TokenStreamComponents(tokenizer);
    }

}
