version=$(printf 'VERSION=${project.version}' | mvn help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')
date=$(date +%Y-%m-%d)

ssh francesco@tcs.inf.tu-dresden.de "rm -rf public_html/conexp-fx/apidocs"
ssh francesco@tcs.inf.tu-dresden.de "mkdir -p public_html/conexp-fx"
scp -r target/apidocs francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/

ssh francesco@tcs.inf.tu-dresden.de "mkdir -p public_html/conexp-fx/download/$version/$date"
scp -r target/conexp-fx* francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/download/$version/$date/
scp -r target/jfx/native/*.dmg francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/download/$version/$date/

ssh francesco@tcs.inf.tu-dresden.de "rm -rf public_html/conexp-fx/webstart"
ssh francesco@tcs.inf.tu-dresden.de "mkdir -p public_html/conexp-fx/webstart"
scp -r target/jfx/native/lib francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/webstart/
scp -r target/jfx/native/web-files francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/webstart/
scp -r target/jfx/native/*.jnlp francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/webstart/
scp -r target/jfx/native/*-jfx.jar francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/webstart/
scp -r target/jfx/native/*.html francesco@tcs.inf.tu-dresden.de:public_html/conexp-fx/webstart/
