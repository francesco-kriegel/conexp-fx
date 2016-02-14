rsync -r target/apidocs francesco@tcs.inf.tu-dresden.de:conexp-fx/ --progress --exclude=.DS_Store --stats --human-readable --delete --delete-excluded --force
rsync target/*.jar francesco@tcs.inf.tu-dresden.de:conexp-fx/ --progress --stats --human-readable --force
rsync target/jfx/native/*.dmg francesco@tcs.inf.tu-dresden.de:conexp-fx/ --progress --stats --human-readable --force
rsync target/jfx/native/*.pkg francesco@tcs.inf.tu-dresden.de:conexp-fx/ --progress --stats --human-readable --force
rsync -r target/web/** francesco@tcs.inf.tu-dresden.de:conexp-fx/webstart --progress --exclude=.DS_Store --stats --human-readable --delete --delete-excluded --force