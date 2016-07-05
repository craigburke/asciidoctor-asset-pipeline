package com.craigburke.asciidoctor

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.CacheManager
import asset.pipeline.fs.AssetResolver
import asset.pipeline.fs.FileSystemAssetResolver
import org.asciidoctor.SafeMode

class AsciidoctorUtil {

    private static ThreadLocal currentAssetPath = new ThreadLocal()

    static String loadAndCacheFile(String path) {
        AssetFile includeFile = AssetHelper.fileForUri(path)
        String content = null
        String sourceFile = currentDocumentPath
        if (includeFile) {
            CacheManager.addCacheDependency(sourceFile, includeFile)
            content = includeFile?.inputStream?.text ?: ''
        }
        content
    }

    static void setCurrentDocumentPath(String documentPath) {
        currentAssetPath.set(documentPath)
    }

    static String getCurrentDocumentPath() {
        currentAssetPath.get() as String
    }

    static Map getAssetPipelineConfig() {
        (AssetPipelineConfigHolder.config?.asciidoctor ?: [:]).asImmutable()
    }

    static Map<String, Object> getConvertOptions() {
        Map options = assetPipelineConfig.collectEntries { String key, val ->
            [(key.replaceAll(/[A-Z]/) { '_' + it[0].toLowerCase() }): val]
        }
        options = getOptionDefaults(options)
        options.attributes = getAttributeDefaults(options.attributes as Map<String, Object>)
        options.asImmutable()
    }

    private static Map<String, Object> getOptionDefaults(Map options) {

        if (options.containsKey('embeddable')) {
            options.header_footer = !options.remove('embeddable')
        }
        else if (!options.containsKey('header_footer')) {
            options.header_footer = true
        }

        if (!options.containsKey('template_dirs')) {
            options.template_dirs = ["${asciidocRoot}/templates" as String]
        }

        options.safe = resolveSafeModeLevel(options.safe)

        options
    }

    private static Map<String, Object> getAttributeDefaults(Map attributes) {
        attributes = attributes ?: [:]

        Map<String, Object> defaultAttributes = [
                base_dir: asciidocRoot,
                docdir: asciidocRoot,
                imagesdir: 'images',
                outdir: asciidocRoot
        ]

        defaultAttributes.each { String key, Object value ->
            if (!attributes.containsKey(key)) {
                attributes[key] = value
            }
        }

        attributes
    }

    private static String getAsciidocRoot() {
        FileSystemAssetResolver appResolver = AssetPipelineConfigHolder.resolvers.find { it instanceof FileSystemAssetResolver && it.name == 'application' }
        "${appResolver.baseDirectory.absolutePath}/asciidoc" as String
    }

    static int resolveSafeModeLevel(Object safe) {
        if (safe == null) {
            0
        }
        else if (safe instanceof Integer) {
            safe
        }
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
