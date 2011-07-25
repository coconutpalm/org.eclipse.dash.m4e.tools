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

java -Dfile.encoding=UTF-8 \
-classpath \
/home/adigulla/workspace/mt4e/target/classes:\
/home/adigulla/.m2/repository/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:\
/home/adigulla/.m2/repository/ch/qos/logback/logback-core/0.9.29/logback-core-0.9.29.jar:\
/home/adigulla/.m2/repository/ch/qos/logback/logback-classic/0.9.29/logback-classic-0.9.29.jar:\
/home/adigulla/.m2/repository/org/codehaus/groovy/groovy-all/1.8.0/groovy-all-1.8.0.jar \
m4e.Tool "$@"
