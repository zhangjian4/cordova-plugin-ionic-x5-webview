var fs = require("fs");
var path = require("path");
const util = require("./util");

module.exports = function (context) {
  console.log("start restore MainActivity.java");
  const root = context.opts.projectRoot;
  const package = util.getPackage(context);
  const mainDir = util.getMainActivityDir(root, package);
  const mainFile = path.join(mainDir, "MainActivity.java");
  const backFile = path.join(mainDir, "MainActivity.java.bak");
  if (fs.existsSync(backFile)) {
    fs.unlinkSync(mainFile);
    fs.renameSync(backFile, mainFile);
    console.log("restore success");
  } else {
    console.log("back file not found,skip");
  }
};
