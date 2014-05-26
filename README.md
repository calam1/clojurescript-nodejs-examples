clojurescript-nodejs-examples
=============================

A collection of clojurescript examples running on nodejs.  

Examples will be categorized by branch names.  The master branch will have the first example and nothing will be merged to that branch.  It is just the initial commit.

Included are simple examples using the following:
- express
- restify
- file I/O
- AWS DynamoDB
  - this resides in the aws-dynamodb branch.  I hope this will help somebody looking for a simple example to interact with AWS (Amazon Web Services).  Although this example is DynamoDB specific, the setup of the aws-sdk and its usage should carry across most if not all AWS services.  I searched high and low for an example of Clojurescript interecting with AWS and found nothing.  Not that it is difficult to interact with the aws-sdk package, just that my level of Clojurescript coding is at beginner level so a lot of things are new and a struggle.
  - a more complicated interaction of aws-dynamodb exists in the aws-dynamodb-create-BOGO-map branch



Thanks for all the hard work of the following posts, blogs, etc and others that I have forgot to acknowledge

- http://dannysu.com/2013/04/14/cljs-restify-node/
- https://gist.github.com/jneira/1171737
- http://caffeinatedideas.com/2013/08/29/taming-nodejs-with-clojurescript.html
- http://andreio.net/clojurescript_with_node_modules_10_Dec_2013.html
