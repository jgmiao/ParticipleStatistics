package org.elasticsearch.index.analysis;

//import org.ansj.lucene.util.AnsjTokenizer;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

import com.secneo.lucene.SecTokenizer;

public class ApknameTokenizerFactory extends AbstractTokenizerFactory {
    // private Environment environment;
    // private Settings settings;

    @Inject
    public ApknameTokenizerFactory(Index index, @IndexSettings Settings indexSettings, Environment env,
            @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        // this.environment = env;
        // this.settings = settings;
        // Dictionary.initial(new Configuration(env));
        // System.out.println("ApkTokenizerFactory()");
    }

    @Override
    public Tokenizer create(Reader reader) {
        // System.out.println("ApkTokenizerFactory.create()");
        Tokenizer tokenizer = null;
        try {
            tokenizer = new SecTokenizer(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokenizer;
    }

}
