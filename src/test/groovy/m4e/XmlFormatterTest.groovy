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
package m4e;

import static org.junit.Assert.*;
import org.junit.Test;

public class XmlFormatterTest {

    @Test
    public void test1() {
        String result = format( '<project />' )
        assertEquals( '<project />', result )
    }

    @Test
    public void test2() {
        String result = format( '<project></project>' )
        assertEquals( '<project></project>', result )
    }

    @Test
    public void test3() {
        String result = format( '<project><a /></project>' )
        assertEquals( '<project>\n  <a />\n</project>', result )
    }

    @Test
    public void test4() {
        String result = format( '<project><a /><b /></project>' )
        assertEquals( '<project>\n  <a />\n  <b />\n</project>', result )
    }

    String format( String input ) {
        def pom = Pom.load( input )

        XmlFormatter formatter = new XmlFormatter( pom: pom )
        formatter.format()

        return pom.toString()
    }
}
