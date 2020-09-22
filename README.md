# About

Dominoes is an approach for analyzing software repositories with thousands of artifacts by considering multiple perspectives of the software development data. We model the data extracted from software repositories and its relationships as matrices, making possible to efficiently process them with a GPUs (Graphics Processing Unit) based architectures.

Dominoes can support automated exploration of different relationships among project artifacts, where users have the flexibility to interactively combine and compose them.

# Team

* Jose Ricardo da Silva Junior (joined in January 2013)
* Leonardo Gresta Paulino Murta (joined in January 2013)
* Esteban Clua (joined in January 2013)
* Anita Sarma (joined in January 2013)
* Daniel Prett (joined in March 2014)

# Documentation
* [Dominoes: An Interactive Exploratory Data Analysis tool for Software Relationships](https://ieeexplore.ieee.org/document/9072287/)
* [Niche vs. breadth: Calculating expertise over time through a fine-grained analysis](http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=7081851&tag=1)
* [Multi-Perspective Exploratory Analysis of Software Development Data](http://www.worldscientific.com/doi/abs/10.1142/S0218194015400033)
* [Exploratory Data Analysis of Software Repositories via GPU Processing](http://ksiresearchorg.ipage.com/seke/seke14paper/seke14paper_173.pdf)

# Usage

In order to ease Dominoes usage, it can be loaded over internet by using the Java Web Start technology. It will automatically detect for a GPU enable device and switch to a CPU processing in case of fail. 

In order to start Dominoes, please follow the steps:

1. Install [Armadillo] (http://arma.sourceforge.net) (linear algebra for CPU processing). It can be installed through a package manger in Linux or Unix using the following commands:

* sudo yum install armadillo (Fedora)
* sudo apt-get install armadillo (Ubuntu)
* [brew](https://github.com/Homebrew/install) install armadillo (OSX)

2. Install CUDA Video Driver for supported Nvidia Graphics Card ([Linux](https://developer.nvidia.com/cuda-downloads) and [OSX](http://www.nvidia.com/object/mac-driver-archive.html)). 

3. Due to the security issues imposed by Java Web Start technology, it is necessary to add a new entry to the exception site list. In order to perform this, open the Java Panel and select **Security** tab. In there, select **"Edit Site List"** button and add http://josericardojunior.com/Dominoes/ site.

4. After adding this site to the list of exception, Dominoes can be loaded directly through [here](http://josericardojunior.com/Dominoes/Dominoes.jnlp).

**Observation**: *Please notice that right now the library is just available for MacOSX and Linux.*

# Development

* [Source Code](https://github.com/gems-uff/dominoes)
* [Issue Tracking](https://github.com/gems-uff/dominoes/issues)

# Technologies

* [Java](http://java.com)
* [JavaFX](http://docs.oracle.com/javafx/)
* [CUDA](http://www.nvidia.com/object/cuda_home_new.html)
* [SQLite](http://www.sqlite.org)

# License

Copyright (c) 2015-2017 Universidade Federal Fluminense (UFF), University of Nebraska-Lincoln (UNL)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
