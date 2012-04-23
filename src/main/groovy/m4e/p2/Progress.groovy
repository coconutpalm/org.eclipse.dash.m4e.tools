package m4e.p2

class ProgressFactory {
    
    Progress newProgress( long contentLength ) {
        return new Progress( contentLength )
    }
}

class Progress {
    long count
    long size
    long sizeInKB
    
    String format = "Downloaded %6d of %6dkb (%3d%%)\r"
    
    public Progress( long size ) {
        this.size = size
        this.sizeInKB = this.size >>> 10
    }
    
    void update( long len ) {
        count += len
        
        int p = size > 0 ? ( 100 * count / size ) : 0
        long progress = count >>> 10
        
        printProgress( progress, p )
    }
    
    void printProgress( long progress, int p ) {
        System.out.printf( format, progress, sizeInKB, p )
        System.out.flush()
    }
    
    void close() {
        System.out.println()
    }
}
