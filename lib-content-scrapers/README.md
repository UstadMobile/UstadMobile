# Content Scraper

A content scraper to find and download content to make them available for offline use


## Setup Chrome Driver for Selenium

if using ubuntu:
use apt-get install chromium-chromedriver

Download the latest chrome driver for your operating system from http://chromedriver.chromium.org/downloads
Unzip the file and copy the file path to the chromedriver 
Open local.properties and add the following line of code:

scraper.chrome_driver_path= path_to_file


## Setup WEbp Lossy

if using ubuntu:
use apt-get install webp

and for other os use:
Download the latest webp compressor for your operating system from https://developers.google.com/speed/webp/docs/precompiled
Unzip the file and copy the filepath to cwebp.exe which is found in the bin folder
Open local.properties and add the following line:

shrinker.webp = path_to_file

or for ubuntu only
use apt-get install webp

## Setup Mogrify

if using ubuntu
apt-get install imagemagick

On Windows:
download from https://imagemagick.org/script/download.php


###Find and Scrap Edraak K12 Content

####To Find All Edraak Content

All the content for edraak K12 is available in the url given below in the gradle task.

>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41" -PfindEdraakDir="C:\edraak\"

Edraak have 6 different categories for courses:

Numbers and processes: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608815f3a50d049abf68e9/?states_program_id=41" -PfindEdraakDir="C:\edraak\"

Alegbra and Patterns: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a6088188c9a02049a3e69e5/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Engineering and Measurement: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608819f3a50d049abf68ea/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Spaces and Sizes: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a608828f3a50d049b1d2cc6/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Statistics and Probability: 
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a60881e6b9064043689772d/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


Triangles:
>$ gradlew scrapeContent -PfindEdraakUrl="https://programs.edraak.org/api/component/5a60881bf3a50d049b1d2cc5/?states_program_id=41" -PfindEdraakDir="C:\edraak\"


#### For Specific Edraak Course Content

To Download a specific exercise or quiz in one of the 6 categories:
Need to use the inspector in the browser for the specific exercise. 

In the Networks Tab, filter by XHR and refresh the page. 2 edraak url will load:- 

1. url for the specific category you are in 
2. url for the course/quiz being loaded 

Take the 2nd link address and enter the gradle task below to download and zip the course.

>$ gradlew scrapeContent -PedraakUrl="https://programs.edraak.org/api/component/5a6087f46380a6049b33fc19/?states_program_id=41" -PedraakDir="C:\edraak\"


### Find and Scrap Phet Simulation

#### Find All Phet Simulations

All supported html simulations can be found at https://phet.colorado.edu/en/simulations/category/html

It will find and download all the simulations and its translations and stores them into a zip file.

>$ gradlew scrapeContent -PfindPhetUrl="https://phet.colorado.edu/en/simulations/category/html" -PfindPhetDir="C:\phet\"

You can also download all the simulations in a specific category:- 
Example link of specific category: https://phet.colorado.edu/en/simulations/category/physics/work-energy-and-power

>$ gradlew scrapeContent -PfindPhetUrl="https://phet.colorado.edu/en/simulations/category/physics/work-energy-and-power" -PfindPhetDir="C:\phet\"



#### Download Specific Phet Simulation

Example: https://phet.colorado.edu/en/simulation/acid-base-solutions

Downloads the simulation and all its translations

>$ gradlew scrapeContent -PphetUrl="https://phet.colorado.edu/en/simulation/acid-base-solutions" -PphetDir="C:\phet\"


#### Find and Scrap All CK12 Content

### Find all CK12 Content

All CK12 Content can be found at https://www.ck12.org/browse/

It will browse each subject to find all its related topics 
and courses and download from each their own variety of content

Ck12 content comes in different format: PLIX, Practice, Video, Read 

To Download All Content:
>$ gradlew scrapeContent -PfindCK12Url="https://www.ck12.org/browse/" -PfindCK12Dir="C:\ck12\"

#### Download Specific Content:

PLIX 
Example: https://www.ck12.org/c/trigonometry/pythagorean-theorem/plix/Find-the-Missing-Side-55c3e84a8e0e082955a19bf6?referrer=concept_details

>$ gradlew scrapeContent -Pck12Url="https://www.ck12.org/c/trigonometry/pythagorean-theorem/plix/Find-the-Missing-Side-55c3e84a8e0e082955a19bf6?referrer=concept_details" -Pck12Dir="C:\ck12\" -Pck12type="plix"

Video Example:
https://www.ck12.org/c/trigonometry/pythagorean-theorem/lecture/The-Pythagorean-Theorem?referrer=concept_details

>$ gradlew scrapeContent -Pck12Url="https://www.ck12.org/c/trigonometry/pythagorean-theorem/lecture/The-Pythagorean-Theorem?referrer=concept_details" -Pck12Dir="C:\ck12\" -Pck12type="video"

READ Example:
https://www.ck12.org/c/trigonometry/pythagorean-theorem/lesson/Lengths-of-Triangle-Sides-Using-the-Pythagorean-Theorem-TRIG?referrer=concept_details

>$ gradlew scrapeContent -Pck12Url="https://www.ck12.org/c/trigonometry/pythagorean-theorem/lesson/Lengths-of-Triangle-Sides-Using-the-Pythagorean-Theorem-TRIG?referrer=concept_details" -Pck12Dir="C:\ck12\" -Pck12type="read"

Practice Example: 
https://www.ck12.org/c/trigonometry/pythagorean-theorem/asmtpractice/Pythagorean-Theorem-Applications-Practice?referrer=featured_content&collectionHandle=trigonometry&collectionCreatorID=3&conceptCollectionHandle=trigonometry-::-pythagorean-theorem?referrer=concept_details

>$ gradlew scrapeContent -Pck12Url="https://www.ck12.org/c/trigonometry/pythagorean-theorem/asmtpractice/Pythagorean-Theorem-Applications-Practice?referrer=featured_content&collectionHandle=trigonometry&collectionCreatorID=3&conceptCollectionHandle=trigonometry-::-pythagorean-theorem?referrer=concept_details" -Pck12Dir="C:\ck12\" -Pck12type="practice"


#### Find all Pratham Books 

All pratham books are found in a single url 

>$ gradlew scrapeContent -PfindPratDir="C:\prathambooks\"


#### Find all African Story Books

All african books are found in a single url 

>$ gradlew scrapeContent -PfindAsbDir="C:\africanbooks\"

#### Find all DDL content

Download all ddl content for all 3 languages

>$ gradlew scrapeContent -PfindDdlUrl="https://www.ddl.af/en/resources" -PfindDdlDir="C:\ddl\"

#### Find all Khan Academy Content

Download all content from Khan Academy 

>$ gradlew scrapeContent -PfindKhanUrl="https://www.khanacademy.org/" -PfindKhanDir="C:\khan\"


#### Find All VOA Content

>$ gradlew scrapeContent -PfindVoaUrl="https://learningenglish.voanews.com/" -PfindVoaDir="C:\voa\"

#### Find All Etekkato Content

>$ gradlew scrapeContent -PfindEtekUrl="http://www.etekkatho.org/subjects/" -PfindEtekDir="C:\etek\"


### Find All Epub in Folder 

>$ gradlew scrapeContent -PfindFolderName="Asafeer" -PfindFolderDir="C:\asafeer\"
