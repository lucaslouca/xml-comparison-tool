# xml-comparison-tool
Quick and dirty Java Swing application to find and match XML files based on certain criteria

## What is this?
This is a small tool that reads and parses all available .xml files in two user definable directories (e.g.: *sample-tickets/Version_1 V12* and *sample-tickets/Version_1 V14*) . Then
for every XML in *sample-tickets/Version_1 V12* it tries to find a *matching* XML in *sample-tickets/Version_1 V14*, based on specified criteria.

In the provided examples an XML `263t373.xml` in the *sample-tickets/Version_1 V12* has a matching xml `70314.xml` because:
* `<test:externalReference>XYZ_00000</test:externalReference>` in `263t373.xml` has a corresponding `<objectId>00000</objectId>` in `70314.xml`
* The sibling element `<test:value>1</test:value>` of `<test:name>IMPORTANT</test:name>` has a corresponding element value `<versionNumber>1</versionNumber>` in `70314.xml`.

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
