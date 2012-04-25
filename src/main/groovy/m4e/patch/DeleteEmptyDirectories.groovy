package m4e.patch

class DeleteEmptyDirectories {

    int counter = 0
    
    int delete( File root ) {
        
        int count = 0
        
        root.eachFile { it ->
            if( it.isDirectory() ) {
                count += delete( it )
            } else {
                count ++
            }
        }
        
        if( 0 == count ) {
            assert root.delete()
            counter ++
        }
        
        return count
    }
}
