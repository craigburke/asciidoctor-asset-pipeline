package com.craigburke.asciidoctor

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import org.asciidoctor.ast.DocumentRuby
import org.asciidoctor.extension.IncludeProcessor
import org.asciidoctor.extension.PreprocessorReader

class AssetPipelineIncludeProcessor extends IncludeProcessor {

    AssetPipelineIncludeProcessor(Map<String, Object> config) {
        super(config)
    }

    @Override
    boolean handles(String target) { true }

    @Override
    void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        String sourceFile = AsciiDoctorProcessor.currentAsset.get().toString()
        AssetFile includeFile = AssetHelper.fileForUri(target)
        if (includeFile) {
            CacheManager.addCacheDependency(sourceFile, includeFile)
            String content = includeFile?.inputStream?.text ?: ''
            reader.push_include(content, target, target, 1, attributes)
        }
    }

}

