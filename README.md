# mn2
ManyNets2 is a network analysis and visualization tool, originally developed at UMD's [HCIL](http://www.cs.umd.edu/hcil/)

### Introduction

ManyNets is a network visualization tool with tabular interface designed to 
visualize up to several thousand network overviews at once.

The academic website of ManyNets is located at 

     [http://www.cs.umd.edu/hcil/manynets](http://www.cs.umd.edu/hcil/manynets)

Academic papers, video demos and presentations can be found there

### Java options

This program is very memory-hungry. You will need around 1.2 Gb to analyze even
moderate datasets (few thousands of nodes with moderate edge density). You 
can change this number by modifying the following file:

    /etc/mn2.conf

And changing the `-Xmx1200m` option in the following line as appropiate. Examples
follow:

    default_options="--branding mn2 -J-Xms24m -J-Xmx1200m"
    default_options="--branding mn2 -J-Xms24m -J-Xmx2048m"
    default_options="--branding mn2 -J-Xms24m -J-Xmx4096m"
    
Notice that the last two examples (using 2Gb and 4Gb) will only work if you have
a 64-bit Windows OS, or are using a Unix-like OS such as Linux or MacOs X. 
Specifying more memory than your system can allocate will result in the
application failing to start.

### License

This program is Open Source, and is distributed under a GPLv3 license, a copy of
which can be found in the "licenses" folder. Third-party libraries included in 
this release have different licenses, all of them open-source, and copies of
each can also be found in the "licenses/" folder. See below for a list of 
included libraries (note that some libraries may in turn use further libraries, 
also released as open-source. Refer to each for further details).

* Prefuse Beta (http://prefuse.org/) - BSD-like
* SocialAction (www.cs.umd.edu/hcil/socialaction/) - GPLv3
* Jython (http://www.jython.org/) - PSF, similar to BSD
* JDom (http://www.jdom.org/) - similar to ALv1
* SwingLabs (https://swinglabs.dev.java.net/) - LGPLv3
* Colt (http://acs.lbl.gov/software/colt/) - Colt License
* Jung (http://jung.sourceforge.net/) - BSD 
* Apache Ant, Commons, Logging, Log4j, Lucene (http://apache.org/) - ALv2
