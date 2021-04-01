const fs = require("fs");
const path = require("path");

function getPackage(context) {
  const root = context.opts.projectRoot;
  const configPath = path.join(root, "/platforms/android/android.json");
  const configStr = fs.readFileSync(configPath, "utf-8");
  const config = JSON.parse(configStr);
  const pluginId = context.opts.plugin.id;
  const package = config.installed_plugins[pluginId].PACKAGE_NAME;
  return package;
}

function getMainActivityDir(root, package) {
  const mainDir = path.join(
    root,
    "platforms/android/app/src/main/java",
    package.replace(/\./g, "/")
  );
  return mainDir;
}

module.exports = {
  getPackage,
  getMainActivityDir
};
