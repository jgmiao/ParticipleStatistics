package org.elasticsearch.index.analysis;

public class ApknameAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        // System.out.println("ApkAnalysisBinderProcessor.processTokenFilters()");
    }

    @Override
    public void processAnalyzers(AnalyzersBindings analyzersBindings) {
        // System.out.println("ApkAnalysisBinderProcessor.processAnalyzers()");
        analyzersBindings.processAnalyzer("apkname", ApknameAnalyzerProvider.class);
        super.processAnalyzers(analyzersBindings);
    }

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        // System.out.println("ApkAnalysisBinderProcessor.processTokenizers()");
        tokenizersBindings.processTokenizer("apkname", ApknameTokenizerFactory.class);
        super.processTokenizers(tokenizersBindings);
    }
}
