package org.elasticsearch.index.analysis;

//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.PropertiesConfiguration;
//import org.apache.commons.lang.builder.ToStringBuilder;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

import com.secneo.lucene.SecAnalyzer;
import com.secneo.participle.util.MyDict;

public class ApknameAnalyzerProvider extends AbstractIndexAnalyzerProvider<SecAnalyzer> {
    private final SecAnalyzer analyzer; 

    @Inject
    public ApknameAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env,
            @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        {
            //只是用这个方法让静态块执行
            @SuppressWarnings("unused")
            int i = MyDict.DEFINITE_ALL_STATE;
        }
        // try {
        // PropertiesConfiguration config = new PropertiesConfiguration("apkname.conf");
        // System.out.println("apkname config: " + ToStringBuilder.reflectionToString(config));
        // MyStaticValue.userLibrary = config.getString("userLibrary", "befo.dic");
        // System.out.println(" MyStaticValue.userLibrary =" + MyStaticValue.userLibrary);
        // MyStaticValue.userLibrary = "/Users/lishijie/Downloads/words/mergefile/befo.dic";
        // } catch (ConfigurationException e) {
        // e.printStackTrace();
        // }
        analyzer = new SecAnalyzer();
        System.out.println("ApkAnalyzerProvider.ApkAnalyzerProvider()");
    }

    @Override
    public SecAnalyzer get() {
        // System.out.println("ApkAnalyzerProvider.get()");
        return this.analyzer;
    }
}
