package com.craigburke.asciidoctor

import static  com.craigburke.asciidoctor.AsciidoctorUtil.*
import org.asciidoctor.extension.JavaExtensionRegistry

import static org.asciidoctor.Asciidoctor.Factory.create
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import org.asciidoctor.Asciidoctor

class AsciidoctorProcessor extends AbstractProcessor {

    private Asciidoctor asciidoctor

    AsciidoctorProcessor(AssetCompiler precompiler) {
        super(precompiler)
        setupAsciidoctor()
        loadProcessors()
    }

    private void setupAsciidoctor() {
        String gemPath = getGemPath()
        asciidoctor = gemPath ? create(gemPath) : create()
        if (assetPipelineConfig.requires) {
            asciidoctor.requireLibraries(assetPipelineConfig.requires as List<String>)
        }
    }

    private void loadProcessors() {
        JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry()
        extensionRegistry.includeProcessor(AssetPipelineIncludeProcessor)
        extensionRegistry.docinfoProcessor(AssetPipelineDocinfoProcessor)

        AssetPipelineDocinfoProcessor footerProcessor = new AssetPipelineDocinfoProcessor([location: ':footer'])
        extensionRegistry.docinfoProcessor(footerProcessor)
    }

    String process(String input, AssetFile assetFile) {
        currentDocumentPath = assetFile.path
        asciidoctor.convert(input, convertOptions)
    }

    static String getGemPath() {
        List<String> pathList = assetPipelineConfig.gemPath ? [assetPipelineConfig.gemPath] : assetPipelineConfig.gemPaths
        String pathSeparator = System.getProperty('path.separator')
        pathList ? pathList.join(pathSeparator) : null
    }
}