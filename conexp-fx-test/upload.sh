mvn clean package -Ptest-on-russell
scp target/conexp-fx-test-*.jar francesco@russell:conexp-fx/conexp-fx-test
