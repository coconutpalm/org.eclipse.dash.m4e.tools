#!/bin/bash

# /*******************************************************************************
# * Copyright (c) 27.04.2012 Aaron Digulla.
# * All rights reserved. This program and the accompanying materials
# * are made available under the terms of the Eclipse Public License v1.0
# * which accompanies this distribution, and is available at
# * http://www.eclipse.org/legal/epl-v10.html
# *
# * Contributors:
# *    Aaron Digulla - initial API and implementation and/or initial documentation
# *******************************************************************************/

set -o pipefail

# Format:
# MissingSources org.eclipse.orbit:orbit.com.google.guava:10.0.1
missingOrbitBundles="$1"

version="R20120119162704"
baseUrl="http://download.eclipse.org/tools/orbit/downloads/drops/${version}/repository"

mapFile="tmp/orbitBundles-${version}.p2.map"

if [[ ! -e "$mapFile" ]]; then
    ( cd tmp ; wget "${baseUrl}/orbitBundles-${version}.p2.map" ; ) || exit 1
fi

while read line ; do
    set -- $( echo "$line" | sed -e 's/:/ /g' )
        
    if [[ "$1" != "MissingSources" ]]; then
        echo "Expected 'MissingSources': ${line}"
        continue
    fi
        
    groupId="$2"
    if [[ "${groupId}" != "org.eclipse.orbit" ]]; then
        echo "Not an orbit bundle -> ignore: ${line}"
        continue
    fi
    
    artifactId=$( echo "$3" | sed -e 's/^orbit\.//' ).source
    version="$4"
    
    echo "Looking for ${groupId}:${artifactId}:${version}"
    
    key="plugin@${artifactId},${version}=p2IU"
    p2line=$(grep "${key}" "${mapFile}")
    rc=$?
    if [[ ${rc} -ne 0 ]]; then
        echo "${line}"
        continue
    fi
    
    bundleVersion=$( echo "${p2line}" | sed -e 's/^.*version=//' -e 's/,.*$//' )
    
    url="$url '${baseUrl}/plugins/${artifactId}_${bundleVersion}.jar'"
done < "${missingOrbitBundles}"

#echo $url

export target=${target:-target}
eval ./run.sh download $url

exit 0
