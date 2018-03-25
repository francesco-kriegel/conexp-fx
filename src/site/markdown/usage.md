# Usage
In case you do not want to run the software within your browser, there is also the option of downloading the latest release for your platform,
either from this GitHub repository, or from the download site at (http://lat.inf.tu-dresden.de/~francesco/conexp-fx/download.html)

The source code of Concept Explorer FX can be used in at least three ways.
1. Add it as a Maven dependency to your Java project.  
2. Start the graphical user interface (class `conexp.fx.gui.ConExpFX`).  
3. Use the command line interface (class `conexp.fx.cli.CLI`).

## System Requirements

CPU: 800 MHz  
RAM: 512 MB  
HDD: 150 MB  
OS: Windows, Linux  
Oracle Java 7 Runtime

## Installation Instructions

### Windows

1\. Download and install Oracle Java 7 Runtime Environment from [http://www.java.com/download](http://www.java.com/download).  
2\. Download and install Concept Explorer FX   
(Uninstall via Windows Control Panel)

### Linux

1\. Download and install Oracle JRE7:  
1.1\. (Optional) Remove OpenJDK:  
`sudo apt-get purge openjdk*  
sudo rm /var/lib/dpkg/info/oracle-java7-installer*  
sudo apt-get purge oracle-java7-installer*  
sudo rm /etc/apt/sources.list.d/*java*  
sudo apt-get update`  

1.2\. Install Oracle Java 7:  
`sudo add-apt-repository ppa:webupd8team/java  
sudo apt-get update  
sudo apt-get install oracle-java7-installer`  

1.3\. Update Configuration:  
Either automatic configuration update (not tested yet):  
`sudo apt-get install oracle-java7-set-default`  
or manually (works on Ubuntu 12.04 LTS):  
`sudo cp /usr/bin/javaws /usr/bin/javaws.old  
sudo gedit /usr/bin/javaws`  
Append or update two lines:  
`JAVA=/usr/lib/jvm/java-7-oracle/jre/bin/java  
CP=/usr/lib/jvm/java-7-oracle/jre/lib/rt.jar`  

source: [http://www.webupd8.org/2012/01/install-oracle-java-jdk-7-in-ubuntu-via.html](http://www.webupd8.org/2012/01/install-oracle-java-jdk-7-in-ubuntu-via.html)  
source: [https://bugs.launchpad.net/ubuntu/+source/icedtea-web/+bug/969520/comments/14](https://bugs.launchpad.net/ubuntu/+source/icedtea-web/+bug/969520/comments/14)  

2\. Download and install ConExpFX  

(Uninstall ConExpFX)  
`sudo dpkg -r conexp-fx`
