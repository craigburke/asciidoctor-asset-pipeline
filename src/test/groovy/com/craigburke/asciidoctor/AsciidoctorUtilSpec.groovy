package com.craigburke.asciidoctor

import spock.lang.Unroll

class AsciidoctorUtilSpec extends AsciidoctorBaseSpec {

    @Unroll
    def "Config can be set to #config"() {
        given:
        AsciidoctorUtil.config == [:]

        when:
        assetPipelineConfig = config

        then:
        AsciidoctorUtil.config.header_footer == headerFooter

        and:
        AsciidoctorUtil.config.safe == safe

        where:
        config                | headerFooter | safe
        [:]                   | true         | 0
        [headerFooter: false] | false        | 0
        [safe: 1]             | true         | 1
    }

    @Unroll
    def "Safe value of #value resolves to #result"() {
        when:
        assetPipelineConfig = [safe: value]

        then:
        AsciidoctorUtil.config.safe == result

        where:
        value    | result
        'unsafe' | 0
        'safe'   | 1
        'server' | 10
        'secure' | 20
        null     | 0
        0        | 0
        1        | 1
        10       | 10
        20       | 20
        99       | 99
        'foo'    | 0
    }

    @Unroll
    def "Header_footer option correctly set when config is #config"() {
        when:
        assetPipelineConfig = config

        then:
        AsciidoctorUtil.config.header_footer == headerFooter

        where:
        config | headerFooter
        [:]                   | true
        [embeddable: false]   | true
        [embeddable: true]    | false
        [headerFooter: true]  | true
        [headerFooter: false] | false
    }

    def "Template_dirs option resolves to #templateDirs"() {
        when:
        assetPipelineConfig = config

        then:
        AsciidoctorUtil.config.template_dirs == templateDirs

        where:
        config                       | templateDirs
        [templateDirs: ['/foo/bar']] | ['/foo/bar']
    }

    def "Default template_dirs is set correctly"() {
        when:
        assetPipelineConfig = [:]

        then:
        AsciidoctorUtil.config.template_dirs == ["${asciidocRoot}/templates" as String]
    }

    def "Default attributes are set correctly"() {
        when:
        assetPipelineConfig = [:]

        then:
        AsciidoctorUtil.config.attributes.base_dir == asciidocRoot

        and:
        AsciidoctorUtil.config.attributes.docdir == asciidocRoot

        and:
        AsciidoctorUtil.config.attributes.imagesdir == 'images'

        and:
        AsciidoctorUtil.config.attributes.outdir == asciidocRoot
    }

    @Unroll
    def "The #attribute attribute can be overridden"() {
        when:
        Map attributes = [(attribute): 'FOOBAR']
        assetPipelineConfig = [attributes: attributes]

        then:
        AsciidoctorUtil.config.attributes[attribute] == 'FOOBAR'

        where:
        attribute << ['base_dir', 'docdir', 'imagesdir', 'outdir']
    }

}
