import { Config } from "@remotion/cli/config";

Config.setVideoImageFormat("jpeg");
Config.setOverwriteOutput(true);
Config.setPublicDir("./remotion/public");
Config.setBrowserExecutable(
  "/root/.cache/puppeteer/chrome/linux-149.0.7827.22/chrome-linux64/chrome"
);
Config.setChromeMode("chrome-for-testing");
