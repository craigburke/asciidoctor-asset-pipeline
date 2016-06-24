package com.craigburke.asciidoctor

import asset.pipeline.AssetPipelineConfigHolder
import org.asciidoctor.extension.JavaExtensionRegistry

import static org.asciidoctor.Asciidoctor.Factory.create
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import org.asciidoctor.Asciidoctor

class AsciiDoctorProcessor extends AbstractProcessor {

    private Asciidoctor asciidoctor
    private static ThreadLocal currentAsset = new ThreadLocal()

    AsciiDoctorProcessor(AssetCompiler precompiler) {
        super(precompiler)
        asciidoctor = create(gemPath as String)
        asciidoctor.requireLibrary('asciidoctor-diagram')
        JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry()
        extensionRegistry.includeProcessor(AssetPipelineIncludeProcessor)
    }

    String process(String input, AssetFile assetFile) {
        currentAsset.set(assetFile.path)
        asciidoctor.convert(input, convertOptions)
    }

    static String getCurrentAssetPath() {
        currentAsset.get().toString()
    }

    static Map getConfig() {
        (AssetPipelineConfigHolder.config?.asciidoctor ?: [:]).asImmutable()
    }

    static String getGemPath() {
        def paths = []
        def config = getConfig()
        def gemPath = config.gemPath
        if (gemPath) {
            paths.add(gemPath)
        }
        else {
            def gemPaths = config.gemPaths
            if (gemPaths) {
                paths.addAll(gemPaths)
            }
        }
        paths.size() ? paths.join(System.getProperty('path.separator')) : null
    }

    static Map<String, Object> getConvertOptions() {
        Map options = [header_footer : true ] + config
        options.asImmutable()
    }

}
