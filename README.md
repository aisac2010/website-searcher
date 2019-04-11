# About
Program downloads a url seed file from
https://s3.amazonaws.com/fieldlens-public/urls.txt
and spawns 20 threads fetches and creates a results.txt file
after searching through the files for a given keyword.

# Building
1. Navigate to the root folder and execute mvn clean install
2. Target folder will be created with regular jar and a larger executable jar

# Running the program
1. cd target
2. java -jar website-searcher-1.0-SNAPSHOT-jar-with-dependencies.jar

This will show the required command line args needed
E.g: java -jar website-searcher-1.0-SNAPSHOT-jar-with-dependencies.jar -k the -o /tmp/ws

At the end of the run the following folders and files will be created
raw : Raw files that the urls points to
text : Parsed files that contain the extracted text
urls.txt : Downloaded urls file
results.txt : Results.txt

# Technologies and Libraries used
- Apache commons libraries (httpclient, io, csv, lang3, cli)
- jsoup to extract text from html
- Google Guice for dependency injection (A minimal DI framework), comes handy during testing with mockito
- Junit with Mockito
- Jococo code coverage

# Tuning the settings
All tunable settings are located src/java/com/websearcher/Constants.java
Notable ones being...
- CONNECTION_TIMEOUT (Currently set at 10 seconds)
- THREAD_COUNT (thread count)

# Known limitations
- Test coverage : Limited due to time constraints (took me a day to finish)
For a real project I would have tried to acheive 95% coverage
- The url file is downloaded each time
