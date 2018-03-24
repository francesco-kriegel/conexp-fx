rm src/main/deploy/security/conexp-fx.jks
keytool -genkeypair -alias conexp-fx -keyalg RSA -validity 36500 -keystore src/main/deploy/security/conexp-fx.jks
keytool -export -alias conexp-fx -file src/main/deploy/security/conexp-fx.cer -keystore src/main/deploy/security/conexp-fx.jks
# openssl x509 -x509toreq -in conexp-fx.crt -out conexp-fx.csr -signkey conexp-fx.key
# openssl req -out conexp-fx.csr -new -newkey rsa:2048 -nodes -keyout conexp-fx.key