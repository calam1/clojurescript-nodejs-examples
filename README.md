clojurescript-nodejs-examples
=============================

This example is a simple clojurescript web app running on nodejs using express.

i.e.
- http://localhost:3000/  - prints hello test on the browser.
- http://localhost:3000/user/test - prints whatever is appended after the user element
- http://localhost:3000/read - reads a file that I hardcoded the location of the file in the code

Notes:

You must have nodejs installed - if you are on a mac just brew install node
You also must have express installed.  Once you have node installed run npm express - npm is installed when you install nodejs

once you compiled this project with the following command - I am using leinengen to manage my projects

lein cljsbuild once

run node nodehello.js (in the directory that nodehello.js resides which should be in the src directory.

credit:
http://andreio.net/clojurescript_with_node_modules_10_Dec_2013.html
