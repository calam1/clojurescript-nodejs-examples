clojurescript-nodejs-examples
=============================

This example is a simple clojurescript web app running on nodejs using express interacting with AWS DynamoDB.

I hope this will help somebody looking for a simple example to interact with AWS (Amazon Web Services).  Although this example is DynamoDB specific, the setup of the aws-sdk and its usage should carry across most if not all AWS services.  I searched high and low for an example of Clojurescript interecting with AWS and found nothing.  Not that it is difficult to interact with the aws-sdk package, just that my level of Clojurescript coding is at beginner level so a lot of things are new and a struggle.

usage of this app:

curl -X POST -H "Content-Type: application/json" -d '{"retailTransactionId": "BG-TRANSACTION-ID-1-5J","dateTime": "2013-05-09","localCurrency": "USD","retailChannel": "WEB","subtotal": 204,"lines": [{"lineSeq": 1,"item": {"productCode": "3m-168","sku": "3m-168-1"},"quantity": 2,"unitPrice": 55,"extendedPrice": 110},{"lineSeq": 2,"item": {"productCode": "3l-168","sku": "3l-168-1"},"quantity": 2,"unitPrice": 47,"extendedPrice": 94}]}' http://localhost:3000/evaluate

Notes:
Amazon account required, and obviously the existence of dynamodb tables.  Your credentials should be exported in your classpath.
You must have nodejs installed - if you are on a mac just brew install node
You also must have express installed.  Once you have node installed run npm express and aws-sdk - npm is installed when you install nodejs

once you compiled this project with the following command - I am using leinengen to manage my projects

lein cljsbuild once

run node dynamodb.js (in the directory that dynamodb.js resides which should be in the src directory.
