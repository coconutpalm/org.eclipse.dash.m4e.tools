package m4e.patch

import de.pdark.decentxml.Element
import m4e.Dependency;
import m4e.Pom;
import m4e.PomUtils;
import m4e.Profile;

/** Tool to work with Maven profiles */
class ProfileTool {
    Pom pom
    String defaultProfileName
    String profileName
    
    Profile defaultProfile
    Profile profile
    
    void replaceDependency( Dependency dependency, PatchDependency replacement ) {
        if( !defaultProfile ) {
            createProfiles()
        }
        
        defaultProfile.addDependency( dependency )
        
        def dep = createDependency( replacement )
        profile.addDependency( dep )
    }
    
    void createProfiles() {
        defaultProfile = pom.profile( defaultProfileName )
        defaultProfile.activeByDefault( true )
        
        profile = pom.profile( profileName )
    }
    
    Dependency createDependency( PatchDependency replacement ) {
        def xml = new Element( 'dependency' )
        
        for( String field in ['groupId', 'artifactId', 'version', 'optional', 'scope'] ) {
            def value = replacement.getProperty( field )
            if( !value ) {
                continue
            }
            
            value = value.toString()
            
            PomUtils.getOrCreate( xml, field ).text = value
        }
        
        return new Dependency( xml: xml, pom: pom )
    }
}
