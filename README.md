# xml-comparison-tool
Quick and dirty Java Swing application to find and match XML files based on certain criteria

## What is this?
This is a small tool that reads and parses all available .xml files in two defined directories (e.g.: *sample-tickets/Version_1 V12* and *sample-tickets/Version_1 V14*) . Then
for every XML in *sample-tickets/Version_1 V12* it tries to find a *matching* XML in *sample-tickets/Version_1 V14*, based on specified criteria.

###How to Import into Eclipse
* **File** -> **Import...** -> **Existing Maven Projects**
* Click **Next**
* Click **Browse...** for the **Root Directory**
* Select and open **xml-comparison-tool**
* Click **Finish**
* Do a mvn update on **xml-comparison-tool**

###How to run
* Run com.lucaslouca.main.TicketAppController.java
* Test the tool using the sample XML files located under *sample-tickets/Version_1 V12* and *sample-tickets/Version_1 V14*

###How to build
* Do a `mvn clean` and then a `mvn package`
* Executables should be in *target*
