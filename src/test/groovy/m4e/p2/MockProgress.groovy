package m4e.p2

class MockProgressFactory extends ProgressFactory {
    @Override
    public Progress newProgress( long contentLength ) {
        return new MockProgress( contentLength );
    }
}

class MockProgress extends Progress {
    MockProgress( long contentLength ) {
        super( contentLength )
    }
    
    @Override
    void printProgress( long progress, int p ) {
        // NOP
    }
    
    @Override
    void close() {
        // NOP
    }
}