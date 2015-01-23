package org.elasticsearch.plugin.analysis.apk;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.ApknameAnalysisBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisApknamePlugin extends AbstractPlugin {

    @Override
    public String name() {
        // System.out.println("AnalysisApkPlugin.name()");
        return "analysis-apk";
    }

    @Override
    public String description() {
        // System.out.println("AnalysisApkPlugin.description()");
        return "apk analysis";
    }

    @Override
    public void processModule(Module module) {
        // System.out.println("AnalysisApkPlugin.processModule()");
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
            analysisModule.addProcessor(new ApknameAnalysisBinderProcessor());
        }
    }
}
