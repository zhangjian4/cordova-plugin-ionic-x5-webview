var fs = require("fs");
var path = require("path");

module.exports = function (context) {
  console.log("start modify MainActivity.java");
  const root = context.opts.projectRoot;
  const configStr = fs.readFileSync(
    root + "/platforms/android/android.json",
    "utf-8"
  );
  const config = JSON.parse(configStr);
  const pluginId = context.opts.plugin.id;
  const package = config.installed_plugins[pluginId].PACKAGE_NAME;
  const mainDir = path.join(
    root,
    "platforms/android/app/src/main/java",
    package.replace(/\./g, "/")
  );
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
