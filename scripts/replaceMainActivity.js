const fs = require("fs");
const path = require("path");
const util = require("./util");

module.exports = function (context) {
  console.log("start modify MainActivity.java");
  const root = context.opts.projectRoot;
  const package = util.getPackage(context);
  const mainDir = util.getMainActivityDir(root, package);
  const mainFile = path.join(mainDir, "MainActivity.java");
  const backFile = path.join(mainDir, "MainActivity.java.bak");
  if (fs.existsSync(backFile)) {
    console.log("back file exists,skip");
  } else {
    console.log("back up to " + backFile);
    const pluginDir = context.opts.plugin.dir;
    fs.renameSync(mainFile, backFile);
    let newMainFile = fs.readFileSync(
      path.join(pluginDir, "src/android/MainActivity.java"),
      "utf-8"
    );
    newMainFile = newMainFile.replace("${package}", package);
    console.log("write a new MainActivity.java");
    fs.writeFileSync(mainFile, newMainFile);
  }
};
