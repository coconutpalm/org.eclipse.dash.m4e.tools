#!/bin/bash

# /*******************************************************************************
# * Copyright (c) 25.07.2011 Aaron Digulla.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *    Aaron Digulla - initial API and implementation and/or initial documentation
# *******************************************************************************/

# Helper script to run the result of "mvn install" 

[[ -e "$PWD/target/classes" ]] || { echo "Missing classes; did you compile?" ; exit 1 ; }

M2_REPO="$HOME/.m2/repository"
DECENT_XML="${M2_REPO}/de/pdark/decentxml/1.4-SNAPSHOT/decentxml-1.4-SNAPSHOT.jar"

[[ -e "$DECENT_XML" ]] || { echo "Missing JARs; did you compile?" ; exit 1 ; }

java -Dfile.encoding=UTF-8 \
-classpath \
$PWD/target/classes:\
${M2_REPO}/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:\
${M2_REPO}/ch/qos/logback/logback-core/0.9.29/logback-core-0.9.29.jar:\
${M2_REPO}/ch/qos/logback/logback-classic/0.9.29/logback-classic-0.9.29.jar:\
${M2_REPO}/org/codehaus/groovy/groovy-all/1.8.1/groovy-all-1.8.1.jar:\
${DECENT_XML} \
m4e.Tool "$@"
