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
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnalyzeCmd extends AbstractCommand {
        
    static final String DESCRIPTION = '''\
repository [ ignore ]
- Check a converted Maven 2 repository for various problems
'''
        
    void doRun( String... args ) {
        if( args.size() <= 1 ) {
            throw new UserError( 'Missing path to repository to analyze' )
        }
        
        File repo = new File( args[1] ).absoluteFile
        if( !repo.exists() ) {
            throw new UserError( "Directory ${repo} doesn't exist" )
        }
        
        File ignoreList = null
        if( args.size() >= 3 ) {
            ignoreList = new File( args[2] ).absoluteFile
            if( !ignoreList.exists() ) {
                throw new UserError( "File with ignore options ${ignoreList} doesn't exist" )
            }
        }
        
        def tool = new Analyzer( repo, Calendar.getInstance() )
        
        if( ignoreList ) {
            tool.loadIgnores( ignoreList )
        }
        
        tool.run()
    }
}

class Analyzer implements CommonConstants {
    
    static final Logger log = LoggerFactory.getLogger( Analyzer )
    
    File repo
    File reportFile
    Calendar timestamp
    Set<Glob> ignores = new HashSet()
    Set<Glob> ignoreMissingSources = new HashSet()
    
    Analyzer( File repo, Calendar timestamp ) {
        this.repo = repo.canonicalFile
        this.timestamp = timestamp
        
        SimpleDateFormat formatter = new SimpleDateFormat( 'yyyyMMdd-HHmmss' )
        formatter.setTimeZone( timestamp.getTimeZone() )
        
        String now = formatter.format( timestamp.getTime() )
        reportFile = new File( repo.absolutePath + "-analysis-${now}.html" )
    }
    
    void loadIgnores( File file ) {
        String manyRegexp = '[^ :]*'
        
        file.eachLine {
            String line = it.substringBefore( '#' ).trim()
            
            if( !line ) {
                return
            }
            
            line = line.replaceAll( '\\s+', ' ' )
            
            if( line.startsWith( 'MissingSources ' ) ) {
                line = line.substringAfter( ' ' )
                ignoreMissingSources << new Glob( line, manyRegexp )
            } else {
                ignores << new Glob( line, manyRegexp )
            }
        }
    }
    
    void run() {
        log.info( 'Analyzing {}...', repo )
        MavenRepositoryTools.eachPom( repo ) {
            analyzePom( it )
        }
        
        loadXmlLogs()
        
        sortEverything()
        
        if( missingSource ) {
            def l = missingSource.findResults {
                def key = it.key()
                
                for( Glob g : ignoreMissingSources ) {
                    if( g.matches( key ) ) {
                        return null
                    }
                }
                
                return it
            }
            
            if( l ) {
                problems << new MissingSources( l )
            }
        }
        
        log.info( 'Found {} POM files. Looking for problems...', poms.size() )
        validate()
        
        log.info( 'Found {} problems. Generating report...', problemCount )
        report()
    }
    
    void loadXmlLogs() {
        File folder = new File( repo, MT4E_FOLDER + '/logs' )
        
        loadXmlLog( folder )
    }
    
    void loadXmlLog( File file ) {
        if( file.isDirectory() ) {
            file.eachFile {
                loadXmlLog( it )
            }
            return
        }

        try {
            log.debug( 'Loading {}', file.absolutePath )
            
            def root = new XmlSlurper().parse( file )
            String path = null
            String command = null
            
            for( def node : root.depthFirst() ) {
                String name = node.name()
                
                switch( name ) {
                case 'source':
                    path = node.'@file'
                    break
                
                case 'mt4e-log':
                    command = node.'@command'
                    break
                
                case 'warning':
                case 'error':
                    xmlToProblem( command, path, node )
                    break
                
                case 'merged':
                    break
                
                default: log.warn( "Unexpected node '${name}'" )
                }
            }
        } catch( Exception e ) {
            throw new RuntimeException( "Error loading XML from ${file.absolutePath}", e )
        }
    }
    
    void xmlToProblem( String command, String path, node ) {
        String code = node.'@code'
        
        def e
        if( 'W' == code[0] ) {
            e = Warning.fromCode( code )
        } else if( 'E' == code[0] ) {
            e = Error.fromCode( code )
        } else {
            throw new RuntimeException( "Unsupported code ${code}" )
        }
        
        def problem
        
        switch( e ) {
        case Error.TWO_VERSIONS:
            problem = TwoVersionsProblem.create( node )
            break
            
        case Error.MISSING_MANIFEST:
            problem = MissingManifest.create( node )
            break
            
        case Error.IMPORT_ERROR:
            problem = ImportError.create( node )
            break
            
        case Warning.MULTIPLE_NESTED_JARS:
            problem = MultipleNestedJarsProblem.create( node )
            break
        
        case Warning.BINARY_DIFFERENCE:
            problem = BinaryDifference.create( node )
            break
            
        default:
            log.warn( "Unsupported code ${code} ${e}" )
            return 
        }
        
        problem.command = command
        problem.logFile = path
        problem.code = code
        
        if( ! problem.message ) {
            problem.message = node.text()
        }
        
        problems << problem
    }
    
    void report() {
//        textReport()
        htmlReport()
    }
    
    void textReport() {
        println "Found ${poms.size()} POM files" // text report
        println "Found ${problems.size()} problems" // text report
        
        for( def p in problems ) {
            println p // text report
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
html, body { background: white; }
.pom { font-weight: bold; color: #7F0055; font-family: monospace; }
.dependency { font-weight: bold; color: #55007F; font-family: monospace; }
.version { font-weight: bold; color: #007F55; font-family: monospace; }
.file { font-weight: bold; color: #00557F; font-family: monospace; }
.files { font-style: italic; }
.padLeft { padding-left: 10px; }
tr:hover { background-color: #D0E0FF; }
.hidden { color: white; }
.error { font-weight: bold; color: red; }
.problem { border-left: 3px solid white; border-bottom: 1px solid #ccc; padding-left: 3px; }
.problem:hover { border-left-color: #cccccc; }
.ignoreKey { color: #ccc; }
'''
                )
                
            }
            body {
                h1 titleText
                
                p "Found ${poms.size()} POM files"
                p "Found ${problems.size()} problems"
                
                renderProblemsAsHtml( builder )
                
                renderRepoAsHtml( builder )
                
                // Add some empty space below the page to make sure anchors can always scroll to the top
                div( style: 'height: 20em;' ) {
                    yield( '&nbsp;', false )
                }
            }
        }
    }
    
    Map<String, String> renderToc( MarkupBuilder builder, List<ProblemType> keys, Map<ProblemType, List<Problem>> map ) {
        
        Map<ProblemType, String> problemTitle2Anchor = [:]
        
        builder.h2 'Table of Contents'
        
        int index = 1
        
        builder.ul( 'class': 'toc' ) {
            for( def key in keys ) {
                String anchor = "toc${index}"
                index ++
                
                problemTitle2Anchor[key] = anchor
                int count = 0
                map[key].each { count += it.problemCount() }
                
                li {
                    a( href: "#${anchor}", "${key.title} (${count})" )
                }
            }
            
            li {
                a( href: "#poms", "${poms.size()} POMs in the repository" )
            }
        }
        
        return problemTitle2Anchor
    }
    
    void renderProblemsAsHtml( MarkupBuilder builder ) {
        Map<ProblemType, List<Problem>> map = [:]
        
        for( def p in problems ) {
            ProblemType type = ProblemType.byClass( p.class )
            def list = map.get( type, [] )
            list << p
        }
        
        List<ProblemType> keys = new ArrayList( map.keySet() )
        keys.sort()
        
        def problemTitle2Anchor = renderToc( builder, keys, map )
        
        for( def key in keys ) {
            def list = map[key]
            
            builder.h2( id: problemTitle2Anchor[key], key.title )
            
            if( key.description ) {
                builder.p key.description
            }
            if( key.url ) {
                builder.p{
                    a( href: key.url, 'More information' )
                }
            }
            
            int count = 0
            list.each { count += it.problemCount() }
            
            String s = count == 1 ? '' : 's'
            builder.p "${count} time${s}"
            
            for( def p in list ) {
                p.render( builder )
            }
        }
    }
    
    void renderRepoAsHtml( MarkupBuilder builder ) {
        builder.h2( id: 'poms', "${poms.size()} POMs in the repository" )
        
        def pomShortKeys = new ArrayList( pomByShortKey.keySet() )
        pomShortKeys.sort()
        
        builder.table( border: '0', cellspacing: '0', cellpadding: '0' ) {
            String currentLabel = ''
            
            tr {
                th 'Group ID'
                th 'Artifact ID + Version'
                th 'Files'
            }
            
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
                    
                    def pom = pomByShortKey[shortKey]
                    def artifactId = pom.value( Pom.ARTIFACT_ID )
                    def version = pom.version()
                    
                    td {
                        span( 'class': 'pom', artifactId )
                        builder.yield( '''<span class='hidden'>:</span>''', false )
                        
                        span( 'class': 'version', version )
                    }
                    
                    td( 'class': 'padLeft' ) {
                        def files = pom.files()
                        
                        if( files ) {
                            span( 'class': 'files' ) {
                                builder.yield( ' ', true )
                                builder.yield( pom.files().join( ' ' ), true )
                            }
                        } else {
                            span( 'class': 'error', 'No files found; check problems above' )
                        }
                    }
                }
            }
        }
    }
    
    void validate() {
        checkDifferentVersions()
        checkMissingDependencies()
        
        postProcessProblemSameKeyDifferentVersion()
        
        applyIgnores()
        
        countProblems()
    }
    
    int problemCount = 0
    
    void countProblems() {
        problems.each { problemCount += it.problemCount() }
    }
    
    void applyIgnores() {
        Set<Glob> unused = new HashSet( ignores )
        
        problems = problems.findResults {
            String key = it.key()
            
            for( Glob g : ignores ) {
                if( g.matches( key ) ) {
                    unused.remove( g )
                    return null
                }
            }
            
            return it
        }
        
        
        if( unused ) {
            log.warn( "Not all ignores were necessary:" )
            unused.each {
                log.warn( '    {}', it )
            }
        }

    }
    
    void postProcessProblemSameKeyDifferentVersion() {
        for( def p in problems ) {
            if( !(p instanceof ProblemSameKeyDifferentVersion ) ) {
                continue
            }
            
            Set<Pom> set = new HashSet<Pom>( nullToEmpty( dependencyUsage[p.pom.shortKey()] ) )
            set.addAll( ( nullToEmpty( dependencyUsage[p.other.shortKey()] ) ) )
            
            List<Pom> list = new ArrayList( set )
            list.sort() { it.key() }
            
            p.usedIn = list
        }
    }

    List nullToEmpty( def list ) {
        return null == list ? [] : list
    }
    
    void checkDifferentVersions() {
        for( def entry in versionBackRefsMap.entrySet() ) {
//            println "${entry.key} -> ${entry.value.keySet()}"
            
            if( entry.value.size() <= 1 ) {
                continue
            }
            
            problems << new ProblemDifferentVersions( entry.key, entry.value )
        }
    }
    
    void checkMissingDependencies() {
        List<String> keys = new ArrayList( dependencyUsage.keySet() )
        keys.sort()

        for( def key in keys ) {
            def pom = pomByShortKey[key]
            
            if( !pom ) {
                problems << new MissingDependency( key, dependencyUsage[key] )
            }
        }
    }
    
    /** List of all POMs in the repo */    
    List<Pom> poms = []
    
    /** shortKey -> POM */
    Map<String, Pom> pomByShortKey = [:]
    
    /** shortKey or dependency -> list of POMs in which it is used */
    Map<String, List<Pom>> dependencyUsage = [:]
    
    /** All found problems */
    List<Problem> problems = []
    
    /** All versions of a dependency */
    Map<String, Set<String>> versions = [:]
    
    /** short key -> versions -> poms */
    Map<String, Map<String, List<Pom>>> versionBackRefsMap = [:]
    
    /** List of artifacts without source */
    List<Pom> missingSource = []

    void sortEverything() {
        poms.sort() { it.key() }
        
        for( def item in dependencyUsage ) {
            item.value.sort() { it.key() }
        }
        
        problems.sort() { it.class.name + ':' + it.sortKey() }
        
        for( def backRefs in versionBackRefsMap ) {
            for( def backRef in backRefs.value ) {
                backRef.value.sort() { it.key() }
            }
        }
        
        missingSource.sort() { it.key() }
    }
    
    void analyzePom( File path ) {
        def pom = Pom.load( path )
        poms << pom
        
        log.debug( 'Analyzing {} {}', path, pom.key() )
        
        File pomPath = MavenRepositoryTools.buildPath( repo, pom.key(), 'pom' ).canonicalFile
        if( path != pomPath ) {
            problems << new PathProblem( pom, pomPath, path )
        }
        
        String shortKey = pom.shortKey()
        Pom other = pomByShortKey[shortKey]
        if( other ) {
            def poms = [ pom, other ].sort { it.key() }
            
            problems << new ProblemSameKeyDifferentVersion( poms[0], poms[1] )
        }
        
        String version = pom.version()
        if( version && version.endsWith( '-SNAPSHOT' ) ) {
            problems << new ProblemSnaphotVersion( pom )
        }
        
        pomByShortKey[shortKey] = pom
        
        def files = pom.files()
        if( files && !files.contains( 'sources' ) ) {
            missingSource << pom
        }
        
        for( def d in pom.dependencies ) {
            
            if( 'true' == d.value( Dependency.OPTIONAL ) ) {
                continue
            }
            
            def depKey = d.shortKey()
            
            def list = dependencyUsage.get( depKey, [] )
            list << pom
            
            version = d.value( Dependency.VERSION )
            if( !version ) {
                problems << new DependencyWithoutVersion( pom, d )
            } else if( '[0,)' == version ) {
                // Ignore
            } else if( isVersionRange( version ) ) {
                // This is no longer a problem because the version ranges are overwritten by dependency management
                // problems << new ProblemVersionRange( pom, d )
            } else if( version && version.endsWith( '-SNAPSHOT' ) ) {
                problems << new ProblemSnaphotVersion( pom, d )
            }
            
            def set = versions.get( depKey, new HashSet<String>() )
            set << version
            
            def versionToPoms = versionBackRefsMap.get( depKey, [:] )
            def backRefs = versionToPoms.get( version, [] )
            backRefs << pom
        }
    }
    
    boolean isVersionRange( String version ) {
        return version && ( 
            version.startsWith( '[' )
            || version.startsWith( '(' )
        )
    }
}

class Problem {
    Pom pom
    String message
    
    Problem( Pom pom, String message ) {
        this.pom = pom
        this.message = message
    }
    
    @Override
    public String toString() {
        return "POM ${pom?.key()}: ${message}"
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            yield( 'POM ', true )
            span( 'class': 'pom', pom.key() )
            yield( ' ', true )
            span( 'class': 'message',  message )
        }
    }
    
    String key() {
        return "${getClass().simpleName} ${pom?.key()}"
    }
    
    String sortKey() {
        return pom.key()
    }
    
    int problemCount() {
        return 1
    }
}

class CommandProblem extends Problem {
    
    String logFile
    String command
    String code
    String key
    
    CommandProblem() {
        super( null, null )
    }
    
    @Override
    public String toString() {
        return message
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            if( key ) {
                div( 'class': 'ignoreKey', key() )
            }
            span( 'class': 'message',  message )
        }
    }
    
    String key() {
        return "${getClass().simpleName} ${key}"
    }
    
    String sortKey() {
        return key
    }
}

class MultipleNestedJarsProblem extends CommandProblem {
    
    String jar
    String nestedJarPath
    String relPath
    
    static MultipleNestedJarsProblem create( node ) {
        
        String jar = PathUtils.normalize( node.'@jar'.toString() )
        String relPath = jar.substringAfterLast( '/m2repo/' )
        String nestedJarPath = node.'@nestedJarPath'
        String key = "${relPath} ${nestedJarPath}"
        
        String message = "Found multiple nested JARs in ${jar}"
        
        def result = new MultipleNestedJarsProblem( jar: jar, nestedJarPath: nestedJarPath, key: key, relPath : relPath, message: message )
        
        return result
    }
}

class TwoVersionsProblem extends CommandProblem {
    
    static TwoVersionsProblem create( node ) {
        
        String shortKey = node.'@shortKey'
        String version1 = node.'@version1'
        String version2 = node.'@version2'
        String key = "${shortKey} ${version1} ${version2}"
        
        String message = "The artifact ${shortKey} exists with several versions: ${version1} ${version2}"
        
        def result = new TwoVersionsProblem( key: key, message: message )
        
        return result
    }
}

class MissingManifest extends CommandProblem {
    
    String jar
    String nestedJarPath
    String relPath
    
    static MissingManifest create( node ) {
        
        String jar = PathUtils.normalize( node.'@jar'.toString() )
        jar = jar.removeEnd( '/META-INF/MANIFEST.MF' )
        
        String key = "${jar.substringAfterLast( '/' )}"
        String message = "Couldn't find MANIFEST.MF in ${jar}"

        def result = new MissingManifest( jar: jar, key: key, message: message )
        
        return result
    }
}

class BinaryDifference extends CommandProblem {
    
    static BinaryDifference create( node ) {
        
        String source = PathUtils.normalize( node.'@source'.toString() )
        String target = PathUtils.normalize( node.'@target'.toString() )
        
        String key = "${source.substringAfterLast( '/' )}"
        
        def result = new BinaryDifference( key: key )
        return result
    }
}

class ImportError extends CommandProblem {
    
    static ImportError create( node ) {
        return new ImportError()
    }
}

class ProblemVersionRange extends Problem {
    
    Dependency dependency
    
    ProblemVersionRange( Pom pom, Dependency dependency ) {
        super( pom, "The dependency ${dependency.key()} in POM ${pom.key()} uses a version range" )
        
        this.dependency = dependency
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message}";
    }
    
    @Override
    String key() {
        return "${super.key()} ${dependency.key()}"
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            yield( 'The dependency ', true )
            span( 'class': 'dependency', dependency.key() )
            yield( ' in POM ', true )
            span( 'class': 'pom', pom.key() )
            yield( ' uses a version range', true )
        }
    }
}

class MissingSources extends Problem {
    
    List<Pom> poms
    
    MissingSources( List<Pom> poms ) {
        super( null, "${poms.size} artifacts are without sources" )
        
        this.poms = poms
    }
    
    @Override
    public String sortKey() {
        return "MissingSources";
    }
    
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder()
        buffer << message
        buffer << ':\n'
        
        poms.each() {
            buffer << "    ${it.key()}\n"
        }
        
        return buffer;
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            p "Missing sources for ${poms.size()} artifacts"
            ul {
                for( def pom in poms ) {
                    li {
                        span( 'class': 'ignoreKey', 'MissingSources ' )
                        span( 'class': 'pom', pom.key() )
                    }
                }
            }
        }
    }
    
    int problemCount() {
        return poms.size()
    }
}

class PathProblem extends Problem {
    
    File expected
    File actual
    
    PathProblem( Pom pom, File expected, File actual ) {
        super( pom, "The path for the POM ${pom.key()} should [${expected}] but it is ${actual} " )
        
        this.expected = expected
        this.actual = actual
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message}";
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            yield( 'The path for the POM ', true )
            span( 'class': 'pom', pom.key() )
            yield( ' should be', true )
            span( 'class': 'file', expected )
            yield( ' but was ', true )
            span( 'class': 'file', actual )
        }
    }
}

class ProblemSnaphotVersion extends Problem {
    
    Dependency dependency
    
    ProblemSnaphotVersion( Pom pom ) {
        super( pom, "The POM ${pom.key()} is a snapshot version" )
    }
    
    ProblemSnaphotVersion( Pom pom, Dependency dependency ) {
        super( pom, "The dependency ${dependency.key()} in POM ${pom.key()} uses a snapshot version" )
        
        this.dependency = dependency
    }
    
    @Override
    public String toString() {
        return "${message}";
    }
    
    void render( MarkupBuilder builder ) {
        if( dependency ) {
            builder.div( 'class': 'problem', 'The dependency ' ) {
                span( 'class': 'dependency', dependency.key() )
                yield( ' in POM ', true )
                span( 'class': 'pom', pom.key() )
                yield( ' uses a snapshot version', true )
            }
        } else {
            builder.div( 'class': 'problem', 'The POM ' ) {
                span( 'class': 'pom', pom.key() )
                yield( ' uses a snapshot version', true )
            }
        }
    }
}

class ProblemSameKeyDifferentVersion extends Problem {
    
    Pom other
    List<Pom> usedIn = []
    
    ProblemSameKeyDifferentVersion( Pom pom, Pom other ) {
        super( pom, 'There is another POM with the same ID but a different version' )
        
        this.other = other
    }
    
    @Override
    String key() {
        return "${super.key()} ${other.key()}"
    }
    
    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message}: ${other.key()}"
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            div( 'class': 'ignoreKey' ) {
                yield( key(), true )
            }
            yield( 'There are two POMs with the same ID but different version:', true )
            ul {
                li {
                    span( 'class': 'pom', pom.key() )
                }
                li {
                    span( 'class': 'pom', other.key() )
                }
            }
            
            if( usedIn ) {
                yield( 'These POMs are used in:', true )
                ul {
                    for( Pom pom in usedIn ) {
                        li {
                            span( 'class': 'pom', pom.key() )
                        }
                    }
                }
            }
        }
    }
}

class DependencyWithoutVersion extends Problem {
    
    Dependency dependency
    
    DependencyWithoutVersion( Pom pom, Dependency dependency ) {
        super( pom, 'Missing version in dependency' )
        
        this.dependency = dependency
    }
    
    @Override
    String key() {
        return "${super.key()} ${dependency.key()}"
    }

    @Override
    public String toString() {
        return "POM ${pom.key()}: ${message} ${dependency.key()}";
    }
    
    void render( MarkupBuilder builder ) {
        builder.div( 'class': 'problem' ) {
            yield( 'POM ', true )
            span( 'class': 'pom', pom.key() )
            yield( ' ', true )
            span( 'class': 'message', message )
            yield( ' ', true )
            span( 'class': 'dependency', dependency.key() )
        }
    }
}

class ProblemDifferentVersions extends Problem {
    
    Map<String, List<Pom>> versionBackRefs
    String dependency
    
    ProblemDifferentVersions( String dependency, Map<String, List<Pom>> versionBackRefs ) {
        super( null, 'This dependency is referenced with different versions' )
        
        this.dependency = dependency
        this.versionBackRefs = versionBackRefs
    }
    
    @Override
    String key() {
        return "${super.key()} ${dependency}"
    }

    @Override
    String sortKey() {
        return dependency
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
            div( 'class': 'ignoreKey' ) {
                yield( key(), true )
            }
            yield( 'The dependency ', true )
            span( 'class': 'dependency', dependency )
            yield( " is referenced with ${versions.size()} different versions:", true )
            
            ul() {
                for( String version in versions ) {
                    renderVersion( builder, version )
                }
            }
        }
    }
    
    void renderVersion( MarkupBuilder builder, String version ) {
        
        def backRefs = versionBackRefs[version]
        backRefs.sort(true) {
            it.key()
        }
        
        builder.li() {
            yield( 'Version "', true )
            span('class': 'version', version)
            yield( '" is used in:', true )
        
            ul() {
                for( def pom in backRefs ) {
                    def parts = pom.key().split(':', -1)
                    
                    li {
                        span('class': 'pom', "${parts[0]}:${parts[1]}")
                        yield( ':', true )
                        span('class': 'version', "${parts[2]}")
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
    }

    @Override
    String sortKey() {
        return key
    }
    
    @Override
    String key() {
        return "${getClass().simpleName} ${key}"
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
            div( 'class': 'ignoreKey' ) {
                yield( key(), true )
            }
            yield( 'The dependency ', true )
            span( 'class':'dependency', key )
            yield( " is used in ${poms.size} POMs:", true )
            
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

enum ProblemType {
    ImportError( 'Plug-ins Which Couldn\'t be Imported', null, Error.IMPORT_ERROR.url() ),
    MissingManifest( 'Plug-ins Without Manifest', 'Each plug-in must have a MANFEST.MF file', Error.MISSING_MANIFEST.url() ),
    BinaryDifference( 'Plug-ins With Binary Differences', 'Each pair of plug-ins should be identical (same name and version) but the JAR files are different', Warning.BINARY_DIFFERENCE.url() ),
    Problem( 'Generic Problems' ),
    ProblemSameKeyDifferentVersion( 'POMs with same ID but different version' ),
    DependencyWithoutVersion( 'Dependencies Without Version' ),
    ProblemDifferentVersions( 'Dependencies With Different Versions' ),
    MissingDependency( 'Missing Dependencies', "The following dependencies are used in POMs in the repository but they couldn't be found in it." ),
    ProblemVersionRange( 'Dependencies With Version Ranges', 'Dependencies should not use version ranges.' ),
    ProblemSnaphotVersion( 'Snapshot Versions', 'Release Repositories should not contain SNAPSHOTs' ),
    PathProblem( 'Path Problems', 'These POMs are not where they should be' ),
    TwoVersionsProblem( 'Artifacts With Several Versions', null, Error.TWO_VERSIONS.url() ),
    MultipleNestedJarsProblem( 'Multiple Nested JARs', "At the moment, MT4E can only handle a single nested JAR", Warning.MULTIPLE_NESTED_JARS.url() ),
    MissingSources( 'Missing Sources' )
    
    final String title
    final String description
    final String url
    
    private ProblemType( String title, String description = null, String url = null ) {
        this.title = title
        this.description = description
        this.url = url
    }
    
    public static ProblemType byClass( Class type ) {
        ProblemType result = valueOf( type.simpleName )
        if( null == result ) {
            throw new RuntimeException( "Unknown type ${type.name}" )
        }
        return result
    }
}
