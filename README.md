clojurescript-nodejs-examples
=============================

This example is a simple clojurescript web app running on nodejs using express.

i.e. http://localhost:8080/hello/test - prints hello test on the browser.

Notes:

You must have nodejs installed - if you are on a mac just brew install node
You also must have express installed.  Once you have node installed run npm express - npm is installed when you install nodejs

once you compiled this project with the following command - I am using leinengen to manage my projects

lein cljsbuild once

run node nodehello.js (in the directory that nodehello.js resides which should be in the src directory.

