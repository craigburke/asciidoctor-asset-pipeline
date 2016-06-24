package com.craigburke.asciidoctor

import asset.pipeline.AssetPipelineConfigHolder
import org.asciidoctor.extension.JavaExtensionRegistry

import static org.asciidoctor.Asciidoctor.Factory.create
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import org.asciidoctor.Asciidoctor
import org.asciidoctor.SafeMode

class AsciidoctorProcessor extends AbstractProcessor {

    private Asciidoctor asciidoctor
    private static ThreadLocal currentAsset = new ThreadLocal()

    AsciidoctorProcessor(AssetCompiler precompiler) {
        super(precompiler)
        asciidoctor = create(gemPath)
        if (config.requires) {
            asciidoctor.requireLibraries(config.requires as List<String>)
        }
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
        List<String> pathList = config.gemPath ? [config.gemPath] : config.gemPaths
        String pathSeparator = System.getProperty('path.separator')
        pathList ? pathList.join(pathSeparator) : null
    }

    static Map<String, Object> getConvertOptions() {
        Map options = config.collectEntries { String key, val ->
            [(key.replaceAll(/[A-Z]/) { '_' + it[0].toLowerCase() }): val]
        }
        if (options.containsKey('embeddable')) {
            options.header_footer = !options.remove('embeddable')
        }
        else if (!options.containsKey('header_footer')) {
            options.header_footer = true
        }
        options.safe = resolveSafeModeLevel(options.safe)
        options.asImmutable()
    }

    static int resolveSafeModeLevel(Object safe) {
        if (safe == null) 0
        else if (safe instanceof Integer) safe
        else {
            try {
                Enum.valueOf(SafeMode, safe.toString().toUpperCase()).level
            }
            catch (IllegalArgumentException e) {
                0
            }
        }
    }
}