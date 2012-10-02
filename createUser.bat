@ECHO OFF

mongo --quiet --eval "db.loadServerScripts(); createUser('%1', '%2');" localhost/mvnrepo
