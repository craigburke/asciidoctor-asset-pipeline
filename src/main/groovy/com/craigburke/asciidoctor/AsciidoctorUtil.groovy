package com.craigburke.asciidoctor

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.CacheManager
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
