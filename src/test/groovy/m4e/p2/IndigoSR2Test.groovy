package m4e.p2;

import static org.junit.Assert.*;

import java.io.File;

import m4e.MopSetup;

import org.junit.Test;

class IndigoSR2Test {
    
    static {
        MopSetup.setup()
    }

    @Test
    public void testParseIndigoSR2() throws Exception {
        def loader = new P2RepoLoader( workDir: new File( PyDevTest.testFolder, "testParseIndigoSR2" ), url: new URL( 'http://download.eclipse.org/releases/indigo/201202240900/' ) )
        def repo = loader.load()
        
        assertEquals( '''\
P2Category( id=Application Development Frameworks, version=0.0.0.67C3cLWJM6, name=Application Development Frameworks )
P2Category( id=Business Intelligence, Reporting and Charting, version=0.0.0.7H7e7AcLUh6hBcMAGMAMGS5sErZT, name=Business Intelligence, Reporting and Charting )
P2Category( id=Collaboration, version=0.0.0.7f8gC_cLS3Q6D3_j23pU4Me0eLz0, name=Collaboration )
P2Category( id=Database Development, version=0.0.0.27G3cLdSS08s73553K5E5ECC1GlM, name=Database Development )
P2Category( id=EclipseRT Target Platform Components, version=0.0.0.7O7a7AcLWqpJBkQGMNXUcQRK1RiD, name=EclipseRT Target Platform Components )
P2Category( id=General Purpose Tools, version=0.0.0.8K81BFcLS3Q6E7R50Bz-XajnO4dz, name=General Purpose Tools )
P2Category( id=Linux Tools, version=0.0.0.17a1cLf7kb7QCJQCQJlQCXQ, name=Linux Tools )
P2Category( id=Mobile and Device Development, version=0.0.0.7b7_CXcLbLnlAUmbhgwfJ1XsKLd4, name=Mobile and Device Development )
P2Category( id=Modeling, version=0.0.0.7z9o7JcLSSHHENJfrFEt_EPhpRDp, name=Modeling )
P2Category( id=Programming Languages, version=0.0.0.887C7AcLTAY6BYkowfw21Gms289g, name=Programming Languages )
P2Category( id=SOA Development, version=0.0.0.7E7H-cLTKnh8MGLFFuRnUEvZ208P, name=SOA Development )
P2Category( id=Testing, version=0.0.0.32-cLY3de, name=Testing )
P2Category( id=Web, XML, Java EE and OSGi Enterprise Development, version=0.0.0.43-cLWd767w31221627022110880, name=Web, XML, Java EE and OSGi Enterprise Development )'''
            , repo.categories.join( '\n' ) )
        assertEquals( 854, repo.features.size() )
        assertEquals( 4541, repo.plugins.size() )
        assertEquals( 114, repo.units.size() )
        
        assertEquals( ''''''
            , repo.others.join( '\n' ) )
        
        //println repo.latest( 'org.eclipse.m2e.feature.feature.group' )
        //println repo.others.join( '\n' )
    }
    
    @Test
    public void testParseIndigoSR2Epps() throws Exception {
        def loader = new P2RepoLoader( workDir: new File( PyDevTest.testFolder, "testParseIndigoSR2Epps" ), url: new URL( 'http://download.eclipse.org/technology/epp/packages/indigo/SR2/' ) )
        def repo = loader.load()
        
        assertEquals( 0, repo.categories.size() )
        assertEquals( 23, repo.features.size() )
        assertEquals( 17, repo.plugins.size() )
        assertEquals( 727, repo.units.size() )
        
        assertEquals( ''''''
            , repo.others.join( '\n' ) )
    }

}
