package com.craigburke.asciidoctor

import asset.pipeline.AbstractAssetFile

class AsciidoctorAssetFile extends AbstractAssetFile {
    static final String contentType = 'text/html'
    static extensions = ['adoc', 'ad', 'asciidoc', 'asc']
    static final String compiledExtension = 'html'

    static processors = [AsciidoctorProcessor]

    static String directiveForLine(String line) {
        return null
    }
}
