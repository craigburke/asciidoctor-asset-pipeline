package com.craigburke.asciidoctor

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.CacheManager
import asset.pipeline.fs.AssetResolver
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.transform.CompileStatic
import org.asciidoctor.SafeMode

/**
 * Asciidoctor config utilities
 * @author Craig Burke
 */
@CompileStatic
class AsciidoctorUtil {

    static final String EMBEDDABLE_KEY = 'embeddable'
    static final String HEADER_FOOTER_KEY = 'header_footer'
    static final String TEMPLATE_DIRS_KEY = 'template_dirs'
    static final String SAFE_KEY = 'safe'

    private static final ThreadLocal CURRENT_ASSET_PATH = new ThreadLocal()

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
        CURRENT_ASSET_PATH.set(documentPath)
    }

    static String getCurrentDocumentPath() {
        CURRENT_ASSET_PATH.get() as String
    }

    static Map<String, Object> getAssetPipelineConfig() {
        Map<String, Object> config = (AssetPipelineConfigHolder.config?.asciidoctor ?: [:]) as Map<String, Object>
        config.asImmutable()
    }

    static Map<String, Object> getConfig() {
        Map options = assetPipelineConfig.collectEntries { String key, val ->
            [(formatConfigKey(key)): val]
        }
        options = getOptionDefaults(options)
        options.attributes = getAttributeDefaults(options.attributes as Map<String, Object>)
        options.asImmutable()
    }

    private static String formatConfigKey(String value) {
        value.replaceAll(/[A-Z]/) { String match -> '_' + match.toLowerCase() }
    }

    static Map<String, Object> getOptionDefaults(Map options) {

        if (options.containsKey(EMBEDDABLE_KEY)) {
            options[HEADER_FOOTER_KEY] = !options.remove(EMBEDDABLE_KEY)
        } else if (!options.containsKey(HEADER_FOOTER_KEY)) {
            options[HEADER_FOOTER_KEY] = true
        }

        if (!options.containsKey(TEMPLATE_DIRS_KEY)) {
            options[TEMPLATE_DIRS_KEY] = ["${asciidocRoot}/templates" as String]
        }

        options[SAFE_KEY] = resolveSafeModeLevel(options[SAFE_KEY])

        options
    }

    static Map<String, Object> getAttributeDefaults(Map attributes) {
        Map<String, Object> result = attributes ?: [:]

        Map<String, String> defaultAttributes = [
                base_dir : asciidocRoot,
                docdir   : asciidocRoot,
                imagesdir: 'images',
                outdir   : asciidocRoot
        ]

        defaultAttributes.each { String key, String value ->
            if (!result.containsKey(key)) {
                result[key] = value
            }
        }

        result
    }

    static String getAsciidocRoot() {
        FileSystemAssetResolver appResolver = AssetPipelineConfigHolder.resolvers
                .find { AssetResolver resolver -> resolver.name == 'application' } as FileSystemAssetResolver
        "${appResolver.baseDirectory.absolutePath}/asciidoc" as String
    }

    static int resolveSafeModeLevel(Object safe) {
        if (safe == null) {
            0
        } else if (safe instanceof Integer) {
            safe as Integer
        } else {
            try {
                Enum.valueOf(SafeMode, safe.toString().toUpperCase()).level
            }
            catch (IllegalArgumentException e) {
                0
            }
        }
    }

}
