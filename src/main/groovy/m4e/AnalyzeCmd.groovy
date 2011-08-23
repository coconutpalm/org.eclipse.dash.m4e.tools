/*******************************************************************************
 * Copyright (c) 23.08.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/
package m4e

import groovy.xml.MarkupBuilder;
import java.io.File;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnalyzeCmd extends AbstractCommand {
        
    static final String DESCRIPTION = '''\
repository - Check a Maven 2 repository for problems
'''
        
    void run( String... args ) {
        if( args.size() == 1 ) {
            throw new UserError( 'Missing path to repository to analyze' )
        }
        
        File repo = new File( args[1] ).absoluteFile
        if( !repo.exists() ) {
            throw new UserError( "Directory ${repo} doesn't exist" )
        }

        new Analyzer( repo, Calendar.getInstance() ).run()
    }
}

class Analyzer {
    
    static final Logger log = LoggerFactory.getLogger( ConsoleUtils )
    
    File repo
    File reportFile
    Calendar timestamp
    
    Analyzer( File repo, Calendar timestamp ) {
        this.repo = repo
        this.timestamp = timestamp
        
        SimpleDateFormat formatter = new SimpleDateFormat( 'yyyyMMdd-HHmmss' )
        formatter.setTimeZone( timestamp.getTimeZone() )
        
        String now = formatter.format( timestamp.getTime() )
        reportFile = new File( repo.absolutePath + "-analysis-${now}.html" )
    }
    
    void run() {
        log.info( 'Analyzing {}...', repo )
        
        analyzeDir( repo )
        
        log.info( 'Found {} POM files. Looking for problems...', pomFiles.size() )
        validate()
        
        log.info( 'Found {} problems. Generating report...', problems.size() )
        report()
    }
    
    void report() {
        textReport()
        htmlReport()
    }
    
    void textReport() {
        println "Found ${pomFiles.size()} POM files"
        println "Found ${problems.size()} problems"
        
        for( def p in problems ) {
            println p
        }
    }
    
    void htmlReport() {
        log.info( 'Writing HTML report to {}', reportFile )
        
        reportFile.withWriter('utf-8') { writer ->
            htmlReport( writer )
        }
    }
    
    void htmlReport( Writer writer ) {
        MarkupBuilder builder = new MarkupBuilder( writer )
        
        SimpleDateFormat formatter = new SimpleDateFormat( 'yyyy.MM.dd HH:mm:ss' )
        formatter.setTimeZone( timestamp.getTimeZone() )
        
        String now = formatter.format( timestamp.getTime() )
        
        String titleText = "Analysis of ${repo} (${now})"
        
        builder.html {
            head {
                title titleText
                
                style( type: 'text/css', '''
.pom { font-weight: bold; color: #7F0055; font-family: monospace; }
.dependency { font-weight: bold; color: #55007F; font-family: monospace; }
.version { font-weight: bold; color: #007F55; font-family: monospace; }
.files { font-style: italic; }
.padLeft { padding-left: 10px; }
tr:hover { background-color: #DFDEF7; }
.hidden { color: white; }
'''
                )
                
            }
            body {
                h1 titleText
                
                p "Found ${pomFiles.size()} POM files"
                p "Found ${problems.size()} problems"
                
                renderProblemsAsHtml( builder )
                renderRepoAsHtml( builder )
            }
        }
    }
    
    Map<String, String> renderToc( MarkupBuilder builder, List<String> keys ) {
        
        Map<String, String> problemTitle2Anchor = [:]
        
        builder.h2 'Table of Contents'
        
        int index = 1
        
        builder.ul( 'class': 'toc' ) {
            for( def key in keys ) {
                String anchor = "toc${index}"
                index ++
                
                problemTitle2Anchor[key] = anchor
                
                li {
                    a( href: "#${anchor}", key )
                }
            }
            
            li {
                a( href: "#poms", 'POMs in the repository' )
            }
        }
        
        return problemTitle2Anchor
    }
    
    void renderProblemsAsHtml( MarkupBuilder builder ) {
        Map<String, List<Problem>> map = [:]
        
        for( def p in problems ) {
            def list = map.get( p.htmlTitle, [] )
            list << p
        }
        
        List<String> keys = new ArrayList( map.keySet() )
        keys.sort()
        
        def problemTitle2Anchor = renderToc( builder, keys )
        
        for( def key in keys ) {
            def list = map[key]
            
            builder.h2 {
                a( name: problemTitle2Anchor[key], key )
            }
            
            for( def p in list ) {
                p.render( builder )
            }
        }
    }
    
    void renderRepoAsHtml( MarkupBuilder builder ) {
        builder.h2 {
            a( name: 'poms', 'POMs in the repository' )
        }
        
        def pomShortKeys = new ArrayList( pomByKey.keySet() )
        pomShortKeys.sort()
        
        builder.table( border: '0', cellspacing: '0', cellpadding: '0' ) {
            String currentLabel = ''
            
            for( def shortKey in pomShortKeys ) {
                String label = shortKey.substringBefore( ':' )
                
                if( label == currentLabel ) {
                    label = ''
                } else {
                    currentLabel = label
                }
                
                tr {
                    td {
                        builder.yield( label, true )
                        
                        if( label ) {
                            builder.yield( '''<span class='hidden'>:</span>''', false )
                        }
                    }
                    
                    def pom = pomByKey[shortKey]
                    def artifactId = pom.value( Pom.ARTIFACT_ID )
                    def version = pom.version()
                    
                    td {
                        span( 'class': 'pom', artifactId )
                        builder.yield( '''<span class='hidden'>:</span>''', false )
                        
                        span( 'class': 'version', version )
                    }
                    
                    td( 'class': 'padLeft' ) {
                        span( 'class': 'files' ) {
                            builder.yield( ' ', true )
                            builder.yield( pom.files().join( ' ' ), true )
                        }
                    }
                }
            }
        }
    }
    
    void validate() {
        checkDifferentVersions()
        checkMissingDependencies()
    }
    
    void checkDifferentVersions() {
        for( def entry in versionBackRefsMap.entrySet() ) {
            if( entry.value.size() <= 1 ) {
                continue
            }
            
            def pom = pomByKey[entry.key]
            if( !pom ) {
                continue
            }
            
            problems << new ProblemDifferentVersions( pom, entry.value )
        }
    }
    
    void checkMissingDependencies() {
        for( def entry in dependencies.entrySet() ) {
            def pom = pomByKey[entry.key]
            
            if( !pom ) {
                problems << new MissingDependency( entry.key, entry.value )
            }
        }
    }
    
    void analyzeDir( File dir ) {
        dir.eachFile() { File it ->
            if( it.isDirectory() ) {
                analyzeDir( it )
            } else if( it.name.endsWith( '.pom' ) ){
                analyzePom( it )
            }
        }
    }
    
    // TODO rename to "poms"
    List<Pom> pomFiles = []
    // TODO rename to pomByShortKey
    Map<String, Pom> pomByKey = [:]
    
    Map<String, List<Pom>> dependencies = [:]
    
    List<Problem> problems = []
    /** All versions of a dependency */
    Map<String, Set<String>> versions = [:]
    /** short key -> version -> poms */
    Map<String, Map<String, List<Pom>>> versionBackRefsMap = [:]
    
    void analyzePom( File path ) {
        def pom = Pom.load( path )
        pomFiles << pom
        
        log.debug( 'Analyzing {} {}', path, pom.key() )
        
        String shortKey = pom.shortKey()
        Pom other = pomByKey[shortKey]
        if( other ) {
            problems << new ProblemSameKeyDifferentVersion( pom, other )
        }
        
        pomByKey[shortKey] = pom
        
        for( def d in pom.dependencies ) {
            def depKey = d.shortKey()
            
            def list = dependencies.get( depKey, [] )
            list << pom
            
            String version = d.value( Dependency.VERSION )
            if( !version || '[0,)' == version ) {
                problems << new ProblemWithDependency( pom, dependency )
            }
            
            def set = versions.get( depKey, new HashSet<String>() )
            set << version
            
            def versionToPoms = versionBackRefsMap.get( depKey, [:] )
            def backRefs = versionToPoms.get( version, [] )
            backRefs << pom
        }
    }
}

class Problem {
    Pom pom
    String htmlTitle = 'Generic Problems'
    String message
    
    Problem( Pom pom, String message ) {
        this.pom = pom
        this.message = message
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message}";
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            ['POM ']
            span( 'class': 'pom', pom.key() )
            [' ']
            span( 'class': 'message',  message )
        }
    }
}

class ProblemSameKeyDifferentVersion extends Problem {
    
    Pom other
    
    ProblemSameKeyDifferentVersion( Pom pom, Pom other ) {
        super( pom, 'There is another POM with the same ID but a different version' )
        
        htmlTitle = 'POMs with same ID but different version'
        
        this.other = other
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message}: ${other.key()}";
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            ['There are two POMs with the same ID but different version:']
            ul {
                li {
                    span( 'class': 'pom', pom.key() )
                }
                li {
                    span( 'class': 'pom', other.key() )
                }
            }
        }
    }
}

// TODO rename to DependencyWithoutVersion
class ProblemWithDependency extends Problem {
    
    Dependency dependency
    
    ProblemWithDependency( Pom pom, Dependency dependency ) {
        super( pom, 'Missing version in dependency' )
        
        this.dependency = dependency
        
        htmlTitle = 'Problems With Dependencies'
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message} ${dependency.key()}";
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            ['POM ']
            span( 'class': 'pom', pom.key() )
            [' ']
            span( 'class': 'message', message )
            [' ']
            span( 'class': 'dependency', dependency.key() )
        }
    }
}

class ProblemDifferentVersions extends Problem {
    
    Map<String, List<Pom>> versionBackRefs
    
    ProblemDifferentVersions( Pom pom, Map<String, List<Pom>> versionBackRefs ) {
        super( pom, 'This dependency is referenced with different versions' )
        
        this.versionBackRefs = versionBackRefs
        
        htmlTitle = 'Dependencies With Different Versions'
    }
    
    @Override
    public String toString() {
        def versions = new ArrayList( versionBackRefs.keySet() )
        Collections.sort( versions )
        
        StringBuilder buffer = new StringBuilder()
        buffer.append( "The dependency ${pom.key()} is referenced with ${versions.size()} different versions:\n" )
        
        for( String version in versions ) {
            buffer.append( "    Version ${version} is used in:\n" )
            
            def backRefs = versionBackRefs[version]
            for( def pom in backRefs ) {
                buffer.append( "        ${pom.key()}" )
            }
        }
        
        return buffer
    }
    
    void render( MarkupBuilder builder ) {
        def versions = new ArrayList( versionBackRefs.keySet() )
        Collections.sort( versions )
        
        builder.div( 'class': 'problem' ) {
            ['The dependency ']
            span( 'class': 'dependency', pom.key() )
            [" is referenced with ${versions.size()} different versions:"]
            ul() {
                for( String version in versions ) {
                    li() {
                        ['Version "']
                        span('class': 'version', version)
                        ['" is used in:']
                    }
                    
                    def backRefs = versionBackRefs[version]
                    backRefs.sort(true) {
                        it.key()
                    }
                    
                    ul() {
                        for( def pom in backRefs ) {
                            def parts = pom.key().split(':', -1)
                            
                            li {
                                span('class': 'pom', "${parts[0]}:${parts[1]}")
                                [':']
                                span('class': 'version', "${parts[2]}")
                            }
                        }
                    }
                }
            }
        }
    }
}

class MissingDependency extends Problem {
    
    String key
    List<Pom> poms
    
    MissingDependency( String key, List<Pom> poms ) {
        super( poms[0], 'Missing dependencies' )
        
        this.key = key
        this.poms = poms
        
        htmlTitle = 'Missing Dependencies'
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder()
        buffer << "The dependency ${key} is used in ${poms.size} POMs but I can't find it in this M2 repo:\n"
        
        for( def pom in poms ) {
            buffer << "    ${pom.key()}"
        }
        
        return buffer
    }
    
    void render(MarkupBuilder builder) {
        builder.div( 'class': 'problem' ) {
            [ 'The dependency ' ]
            span( 'class':'dependency', key )
            [ " is used in ${poms.size} POMs but I can't find it in this M2 repo" ]
            
            ul {
                for( def pom in poms ) {
                    li {
                        span( 'class': 'pom', pom.key() )
                    }
                }
            }
        }
	}
}
