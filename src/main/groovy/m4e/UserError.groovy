/*******************************************************************************
 * Copyright (c) 25.07.2011 Aaron Digulla.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Aaron Digulla - initial API and implementation and/or initial documentation
 *******************************************************************************/

package m4e

class UserError extends RuntimeException {

    UserError( String message ) {
        super( message );
    }
    
    UserError( String message, Throwable cause ) {
        super( message, cause );
    }
    
    void print() {
        System.err.println( message );
        if( cause ) {
            cause.printStackTrace();
        }
    }
}
