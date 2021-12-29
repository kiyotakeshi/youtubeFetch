# youtube-fetch

## Build

```shell
$ export JAVA_HOME=`/usr/libexec/java_home -v 11`

$ java -version                                  
openjdk version "11.0.8" 2020-07-14

$ mvn clean package

$ ls -l 1/target/youtube-fetch1-1.0.0.jar     
-rw-r--r--  1 kiyotakeshi  staff  5804 Dec 27 00:59 1/target/youtube-fetch1-1.0.0.jar
```

## Run

```shell
$ export YOUTUBE_APIKEY="AIzaShogefugahogefugahogefugahogefugaho"

# Display latest 100 videos URL.
$ java -jar 1/target/youtube-fetch1-1.0.0.jar [search word] 

# Display the posted within 3 days Japanese video top 10
$ java -jar 2/target/youtube-fetch2-1.0.0.jar [search word] 
```
