# Search Among Sensitive Content

## System Requirements
- To **compile** the app with all of its dependencies, you need to install **Java 8 JDK** and **Apache Maven**. [Here](https://www.twilio.com/blog/2017/01/install-java-8-apache-maven-google-web-toolkit-windows-10.html) is a tutorial that shows how to do so on Windows 10 (you do **not** need to do the third step of installing Google Web Toolkit).
- To **run** the app, you only need **Java 8 JRE**. That is you can copy the app from a machine (on which the app was compiled) to another machine that has the JRE without JDK nor Maven.

## Installation
    git clone https://github.com/mossaab0/sasc.git
    cd sasc
    MAVEN_OPTS="-Xmx512m" mvn install

## Launching the Application

### On MS Windows
    Double click on sasc.bat

### On Mac and GNU/Linux
	./sasc.sh