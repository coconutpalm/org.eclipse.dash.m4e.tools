package m4e.p2;

import java.net.URL;

class P2DownloadException extends P2Exception {

    final URL url
    final List<URL> urls
    
    P2DownloadException( URL url, List<URL> urls ) {
        super( "Unable to download ${url} from\n${urls?.join( '\n' )}" )
        
        this.url = url
        this.urls = urls
    }
    
    P2DownloadException( URL url ) {
        super( "Unable to download ${url} (cached)" )
        
        this.url = url
    }
}
