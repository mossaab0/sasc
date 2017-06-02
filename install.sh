cd ..
# Download a set of helper libraries I developed
git clone https://github.com/mossaab0/tools.git

# A library for reading and writing files (including CSV, GZIP and BZ2).
cd tools/tools-io
mvn install

# A library for language operations (e.g., stemming, n-gram generation).
cd ../tools-lang
mvn install
cd ../..

# Once those libraries are installed locally (i.e. in ~/.m2), we don't need to keep the source.
rm -rf tools

cd search-in-secrets
mvn install