#!/usr/bin/env node

const { execSync } = require("child_process");
const fs = require("fs");
const path = require("path");

const projectRoot = path.resolve(__dirname);
const androidProjectRoot = path.resolve(projectRoot, "android");

// Create React Native bundle
console.log("Creating React Native bundle...");
execSync(
  "npx react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle --assets-dest android/app/src/main/res",
  {
    cwd: projectRoot,
    stdio: "inherit",
  }
);

console.log("React Native bundle created successfully!");
