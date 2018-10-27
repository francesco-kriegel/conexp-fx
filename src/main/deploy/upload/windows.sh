echo "Uploading Windows Bundles..."

version=$(printf 'VERSION=${project.version}' | mvn help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')
date=$(date +%Y-%m-%d)

ssh francesco@tcs.inf.tu-dresden.de "mkdir -p public_html/conexp-fx/download/$version/$date"
ls target/jfx/native/
scp -r target/jfx/native/*.msi francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/download/$version/$date/
