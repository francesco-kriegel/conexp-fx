C:
cd "C:\.dev\workspace\dev64\conexp\conexp-fx\key"
del /f keystore.jks
"%JAVA_HOME%\bin\keytool" -genkeypair -alias conexp-fx -keyalg RSA -validity 36500 -keystore conexp-fx.jks
"%JAVA_HOME%\bin\keytool" -export -alias conexp-fx -file conexp-fx.crt -keystore conexp-fx.jks