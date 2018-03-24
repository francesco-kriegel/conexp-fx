version=$(printf 'VERSION=${project.version}' | mvn help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')
date=$(date +%Y-%m-%d)

ssh francesco@tcs.inf.tu-dresden.de "mkdir -p public_html/conexp-fx/download/$version/$date"
scp -r target/conexp-fx* francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/download/$version/$date/
scp -r target/jfx/native/*.msi francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/download/$version/$date/
