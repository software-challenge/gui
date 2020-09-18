const fs = require('fs');
const core = require('@actions/core');
const github = require('@actions/github');

try {
  var files = fs.readdirSync('build').filter(fn => fn.endsWith('.jar'));
  console.log("files are: ", files)
  const path = "build/" + files[0];
  console.log("jar path will be ", path)
  core.setOutput("jar-path", path);
  // Get the JSON webhook payload for the event that triggered the workflow
  const payload = JSON.stringify(github.context.payload, undefined, 2);
  console.log(`The event payload: ${payload}`);
} catch (error) {
  core.setFailed(error.message);
}
