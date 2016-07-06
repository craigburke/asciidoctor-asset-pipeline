package com.craigburke.asciidoctor

import static org.asciidoctor.Asciidoctor.Factory.create

import groovy.transform.CompileStatic
import org.asciidoctor.extension.JavaExtensionRegistry
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import org.asciidoctor.Asciidoctor

/**
 * Asciidoctor processor for Asciidoctor asset files
 * @author Craig Burke
 */
@CompileStatic
class AsciidoctorProcessor extends AbstractProcessor {

    private Asciidoctor asciidoctor

    AsciidoctorProcessor(AssetCompiler precompiler) {
        super(precompiler)
        setupAsciidoctor()
        loadProcessors()
    }

    private void setupAsciidoctor() {
        asciidoctor = gemPath ? create(gemPath) : create()
        if (AsciidoctorUtil.assetPipelineConfig.requires) {
            asciidoctor.requireLibraries(AsciidoctorUtil.assetPipelineConfig.requires as List<String>)
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
        AsciidoctorUtil.currentDocumentPath = assetFile.path
        asciidoctor.convert(input, AsciidoctorUtil.config)
    }

    static String getGemPath() {
        List<String> pathList = AsciidoctorUtil.assetPipelineConfig.gemPath ?
                [AsciidoctorUtil.assetPipelineConfig.gemPath as String] :
                AsciidoctorUtil.assetPipelineConfig.gemPaths as List<String>

        String pathSeparator = System.getProperty('path.separator')
        pathList ? pathList.join(pathSeparator) : null
    }
}
