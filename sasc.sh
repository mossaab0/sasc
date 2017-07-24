#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2001-2006 The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
#   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
#   reserved.


# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR/.." >/dev/null; pwd`

# Reset the REPO variable. If you need to influence this use the environment setup file.
REPO=target/repo


# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
		   if [ -z "$JAVA_HOME" ]; then
		      if [ -x "/usr/libexec/java_home" ]; then
			      JAVA_HOME=`/usr/libexec/java_home`
			  else
			      JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
			  fi
           fi       
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." 1>&2
  echo "  We cannot execute $JAVACMD" 1>&2
  exit 1
fi

if [ -z "$REPO" ]
then
  REPO="$BASEDIR"/repo
fi

CLASSPATH="$BASEDIR"/etc:"$REPO"/tomcat-embed-core-8.5.16.jar:"$REPO"/tomcat-embed-logging-juli-8.5.2.jar:"$REPO"/tomcat-embed-jasper-8.5.16.jar:"$REPO"/tomcat-embed-el-8.5.16.jar:"$REPO"/ecj-3.12.3.jar:"$REPO"/javax.mail-1.5.6.jar:"$REPO"/activation-1.1.jar:"$REPO"/tika-core-1.15.jar:"$REPO"/tika-parsers-1.15.jar:"$REPO"/vorbis-java-tika-0.8.jar:"$REPO"/jackcess-2.1.4.jar:"$REPO"/jackcess-encrypt-2.1.1.jar:"$REPO"/jmatio-1.2.jar:"$REPO"/apache-mime4j-core-0.7.2.jar:"$REPO"/apache-mime4j-dom-0.7.2.jar:"$REPO"/commons-compress-1.13.jar:"$REPO"/xz-1.6.jar:"$REPO"/commons-codec-1.10.jar:"$REPO"/pdfbox-2.0.6.jar:"$REPO"/fontbox-2.0.6.jar:"$REPO"/pdfbox-tools-2.0.6.jar:"$REPO"/pdfbox-debugger-2.0.6.jar:"$REPO"/jempbox-1.8.13.jar:"$REPO"/bcmail-jdk15on-1.54.jar:"$REPO"/bcpkix-jdk15on-1.54.jar:"$REPO"/bcprov-jdk15on-1.54.jar:"$REPO"/poi-3.16.jar:"$REPO"/commons-collections4-4.1.jar:"$REPO"/poi-scratchpad-3.16.jar:"$REPO"/poi-ooxml-3.16.jar:"$REPO"/poi-ooxml-schemas-3.16.jar:"$REPO"/xmlbeans-2.6.0.jar:"$REPO"/curvesapi-1.04.jar:"$REPO"/tagsoup-1.2.1.jar:"$REPO"/asm-5.0.4.jar:"$REPO"/isoparser-1.1.18.jar:"$REPO"/metadata-extractor-2.9.1.jar:"$REPO"/xmpcore-5.1.2.jar:"$REPO"/boilerpipe-1.1.0.jar:"$REPO"/rome-1.5.1.jar:"$REPO"/rome-utils-1.5.1.jar:"$REPO"/vorbis-java-core-0.8.jar:"$REPO"/juniversalchardet-1.0.3.jar:"$REPO"/jhighlight-1.0.2.jar:"$REPO"/java-libpst-0.8.1.jar:"$REPO"/junrar-0.7.jar:"$REPO"/commons-vfs2-2.0.jar:"$REPO"/maven-scm-api-1.4.jar:"$REPO"/plexus-utils-1.5.6.jar:"$REPO"/maven-scm-provider-svnexe-1.4.jar:"$REPO"/maven-scm-provider-svn-commons-1.4.jar:"$REPO"/regexp-1.3.jar:"$REPO"/cxf-rt-rs-client-3.0.12.jar:"$REPO"/cxf-rt-transports-http-3.0.12.jar:"$REPO"/cxf-core-3.0.12.jar:"$REPO"/woodstox-core-asl-4.4.1.jar:"$REPO"/stax2-api-3.1.4.jar:"$REPO"/xmlschema-core-2.2.1.jar:"$REPO"/cxf-rt-frontend-jaxrs-3.0.12.jar:"$REPO"/javax.ws.rs-api-2.0.1.jar:"$REPO"/javax.annotation-api-1.2.jar:"$REPO"/commons-exec-1.3.jar:"$REPO"/opennlp-tools-1.6.0.jar:"$REPO"/commons-io-2.5.jar:"$REPO"/json-simple-1.1.1.jar:"$REPO"/json-20140107.jar:"$REPO"/gson-2.2.4.jar:"$REPO"/slf4j-api-1.7.24.jar:"$REPO"/jul-to-slf4j-1.7.24.jar:"$REPO"/jcl-over-slf4j-1.7.24.jar:"$REPO"/netcdf4-4.5.5.jar:"$REPO"/jcip-annotations-1.0.jar:"$REPO"/jna-4.1.0.jar:"$REPO"/grib-4.5.5.jar:"$REPO"/protobuf-java-2.5.0.jar:"$REPO"/jdom2-2.0.4.jar:"$REPO"/jsoup-1.7.2.jar:"$REPO"/jj2000-5.2.jar:"$REPO"/bzip2-0.9.1.jar:"$REPO"/cdm-4.5.5.jar:"$REPO"/udunits-4.5.5.jar:"$REPO"/httpcore-4.2.5.jar:"$REPO"/joda-time-2.2.jar:"$REPO"/quartz-2.2.0.jar:"$REPO"/c3p0-0.9.1.1.jar:"$REPO"/ehcache-core-2.6.2.jar:"$REPO"/guava-17.0.jar:"$REPO"/httpservices-4.5.5.jar:"$REPO"/httpclient-4.2.6.jar:"$REPO"/httpmime-4.2.6.jar:"$REPO"/commons-csv-1.0.jar:"$REPO"/sis-utility-0.6.jar:"$REPO"/sis-netcdf-0.6.jar:"$REPO"/sis-storage-0.6.jar:"$REPO"/sis-referencing-0.6.jar:"$REPO"/sis-metadata-0.6.jar:"$REPO"/geoapi-3.0.0.jar:"$REPO"/jsr-275-0.9.3.jar:"$REPO"/sentiment-analysis-parser-0.1.jar:"$REPO"/tika-langdetect-1.13.jar:"$REPO"/language-detector-0.5.jar:"$REPO"/jsonic-1.2.11.jar:"$REPO"/annotations-12.0.jar:"$REPO"/jackson-core-2.8.1.jar:"$REPO"/mstor-0.9.13.jar:"$REPO"/commons-lang-2.4.jar:"$REPO"/commons-net-1.4.1.jar:"$REPO"/oro-2.0.8.jar:"$REPO"/jdom-1.0.jar:"$REPO"/xstream-1.2.2.jar:"$REPO"/xpp3_min-1.1.3.4.O.jar:"$REPO"/jyaml-1.3.jar:"$REPO"/jcr-2.0.jar:"$REPO"/jackrabbit-jcr-rmi-2.2.8.jar:"$REPO"/jcrom-1.3.2.jar:"$REPO"/ehcache-1.4.1.jar:"$REPO"/jsr107cache-1.0.jar:"$REPO"/backport-util-concurrent-3.1.jar:"$REPO"/commons-logging-1.0.4.jar:"$REPO"/commons-collections-3.2.jar:"$REPO"/cglib-2.2.jar:"$REPO"/jcharset-2.0.jar:"$REPO"/jutf7-1.0.0.jar:"$REPO"/jcommander-1.72.jar:"$REPO"/lucene-queries-6.6.0.jar:"$REPO"/lucene-core-6.6.0.jar:"$REPO"/lucene-queryparser-6.6.0.jar:"$REPO"/lucene-sandbox-6.6.0.jar:"$REPO"/lucene-analyzers-common-6.6.0.jar:"$REPO"/commons-lang3-3.6.jar:"$REPO"/jsf-impl-2.2.14.jar:"$REPO"/jsf-api-2.2.14.jar:"$REPO"/jstl-api-1.2-rev-1.jar:"$REPO"/jsp-api-2.1.jar:"$REPO"/primefaces-6.1.jar:"$REPO"/commons-fileupload-1.3.3.jar:"$REPO"/cdi-api-2.0.jar:"$REPO"/javax.el-api-3.0.0.jar:"$REPO"/javax.interceptor-api-1.2.jar:"$REPO"/javax.inject-1.jar:"$REPO"/search-in-secrets-1.0-SNAPSHOT.jar

ENDORSED_DIR=
if [ -n "$ENDORSED_DIR" ] ; then
  CLASSPATH=$BASEDIR/$ENDORSED_DIR/*:$CLASSPATH
fi

if [ -n "$CLASSPATH_PREFIX" ] ; then
  CLASSPATH=$CLASSPATH_PREFIX:$CLASSPATH
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$HOME" ] && HOME=`cygpath --path --windows "$HOME"`
  [ -n "$BASEDIR" ] && BASEDIR=`cygpath --path --windows "$BASEDIR"`
  [ -n "$REPO" ] && REPO=`cygpath --path --windows "$REPO"`
fi

exec "$JAVACMD" $JAVA_OPTS  \
  -classpath "$CLASSPATH" \
  -Dapp.name="webapp" \
  -Dapp.pid="$$" \
  -Dapp.repo="$REPO" \
  -Dapp.home="$BASEDIR" \
  -Dbasedir="$BASEDIR" \
  edu.umd.umiacs.clip.sis.TomcatLauncher \
  "$@"
