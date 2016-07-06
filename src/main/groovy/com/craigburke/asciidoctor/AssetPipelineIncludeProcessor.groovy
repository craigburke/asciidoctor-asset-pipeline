package com.craigburke.asciidoctor

import groovy.transform.CompileStatic
import org.asciidoctor.ast.DocumentRuby
import org.asciidoctor.extension.IncludeProcessor
import org.asciidoctor.extension.PreprocessorReader

/**
 * Include processor for the Asset Pipeline
 * @author Craig Burke
 */
@CompileStatic
class AssetPipelineIncludeProcessor extends IncludeProcessor {

    AssetPipelineIncludeProcessor(Map<String, Object> config) {
        super(config)
    }

    @Override
    boolean handles(String target) { true }

    @Override
    void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        String content = AsciidoctorUtil.loadAndCacheFile(target)
        if (content) {
            reader.push_include(processTags(content, attributes), target, target, 1, attributes)
        }
    }

    private String processTags(String content, Map<String, Object> attributes) {
        String tagList = attributes.tags

        if (tagList) {
            StringBuilder sb = new StringBuilder()
            List<String> lines = content.readLines()

            tagList.tokenize(';').each { String tag ->
                int startTagIndex = lines.findIndexOf { String line -> line.contains("tag::${tag}[]") }
                if (startTagIndex > -1) {
                    int endTagIndex = lines.findIndexOf { String line -> line.contains("end::${tag}[]") }

                    if (endTagIndex == -1) {
                        throw new RuntimeException("No closing tag [${tag}] found in document!")
                    }

                    sb = sb.append(lines[(startTagIndex + 1)..(endTagIndex - 1)].join('\n'))
                } else {
                    throw new RuntimeException("Tag [${tag}] not found in document!")
                }
            }

            sb.toString()
        } else {
            content
        }
    }

}

