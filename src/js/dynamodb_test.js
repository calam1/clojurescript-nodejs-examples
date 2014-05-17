var AWS = require('aws-sdk');
AWS.config.region = 'us-east-1';

var db = new AWS.DynamoDB();
db.listTables (function (err, data){
  console.log(data.TableNames);
})

var params = {"TableName" : "commerce.business.promote.PRODUCT"}
db.describeTable(params, function (err, data){
if (err) console.log(err, err.stack);
else console.log(data);
})
