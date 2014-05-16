clojurescript-nodejs-examples
=============================

This example is a simple clojurescript web app running on nodejs using express.

i.e.
- http://localhost:8080/tables  - prints all your dynamodb tables to the browser.

Notes:
Amazon account required, and obviously the existence of dynamodb tables.  Your credentials should be exported in your classpath.
You must have nodejs installed - if you are on a mac just brew install node
You also must have express installed.  Once you have node installed run npm express - npm is installed when you install nodejs

once you compiled this project with the following command - I am using leinengen to manage my projects

lein cljsbuild once

run node dynamodb.js (in the directory that dynamodb.js resides which should be in the src directory.
