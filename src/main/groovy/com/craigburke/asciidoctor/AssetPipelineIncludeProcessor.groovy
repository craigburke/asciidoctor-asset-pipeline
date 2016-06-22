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
        String sourceFile = AsciiDoctorProcessor.currentAssetPath
        AssetFile includeFile = AssetHelper.fileForUri(target)
        if (includeFile) {
            CacheManager.addCacheDependency(sourceFile, includeFile)
            String content = includeFile?.inputStream?.text ?: ''
            reader.push_include(preprocess(content, attributes), target, target, 1, attributes)
        }
    }

    private String preprocess(String content, Map<String, Object> attributes) {
        StringBuilder sb = new StringBuilder()
        if (attributes.containsKey('tags')) {
            def tags = attributes.tags.split(/;|,/)
            def lines = content.readLines()
            tags.each { tag ->
                def startTagIndex = lines.findIndexOf { it.contains("tag::${tag}[]") }
                if (startTagIndex > -1) {
                    def endTagIndex = lines.findIndexOf { it.contains("end::${tag}[]") }
                    if (endTagIndex == -1) {
                        throw new RuntimeException("No closing tag [${tag}] found in document!")
                    }
                    sb = sb.append(lines[(startTagIndex+1)..(endTagIndex-1)].join('\n'))
                } else {
                    throw new RuntimeException("Tag [${tag}] not found in document!")
                }
            }
        } else {
            sb = sb.append(content)
        }
        return sb.toString()
    }

}

