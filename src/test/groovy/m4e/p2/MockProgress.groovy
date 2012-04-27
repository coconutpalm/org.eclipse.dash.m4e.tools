/*******************************************************************************
 * Copyright (c) 23.04.2012 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

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