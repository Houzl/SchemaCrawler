rm -f database-dump.html
java -classpath ../_schemacrawler/lib/schemacrawler-8.4.jar:../_schemacrawler/lib/hsqldb-2.0.0.jar schemacrawler.Main -c hsqldb -infolevel=standard -command=count,dump -outputformat=html -outputfile=database-dump.html
echo Database dump is in database-dump.html