# Wiki-Olympics-Scraper
This program will help us to query the depths of Wikipedia.
All you need to do is run the src/main/java/Querybase.java main function, and you will be
taken into a Q&A session where we will do the best of our abilities
to provide you with the most accurate response.
The other files provide an under the hood look of how we are implementing our
information finding logic.

Assumptions:
1. While we assume that the user will be well-behaved, the code is robust enough to handle any
   unexpected user input.
2. I am assuming that the structure of Wikipedia is going to stay fairly stagnant over the foreseeable
   future. If there are changes in Wikipedia pages, the following may be affected:
   a. Question 1 assumes that the table of countries will remain the first table
   b. Question 2 assumes that the table with obsolete olympic nations is the first table on the page
   c. Question 3 assumes that the medal table for a specific year will be the first table on the page.
   d. Question 4 assumes that the title of the podium sweep section will always be an h3 element of title Podium_sweeps
   e. Question 5 assumes that the medal table will be of heading h2 and title Medal table. But it is more
   flexible in checking for other heading types as well. However, if heading title changes may cause problems.
   f. Question 6 assumes that the table containing links to the sports pages is the first table on the homepage.
   g. Question 7 also assumes that the relevant table is the first on the page.
   h. Question 8 assumes that the date of death will have the title "Died" and that if someone is not alive,
   then they will not have any entry with the word "Died" in the respective infobox table.

Challenges:
1. It was tedious to manually inspect the HTML elements of all pages in order to determine a scraping
   scheme.
2. The API call to openStreetMap API became a bottleneck in some of my code.
3. Reading javadocs was a tedious task as well, but was good practice for the future.

Feel free to try the code and experiment with various inputs. This code is reliable.

