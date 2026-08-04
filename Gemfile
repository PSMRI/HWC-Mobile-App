source "https://rubygems.org"

# Keep the CI toolchain reproducible. The Firebase App Distribution plugin
# 1.0.0 uses the current Firebase App Distribution API client and requires
# Fastlane 2.232.0 or newer.
gem "fastlane", "2.232.0"

plugins_path = File.join(File.dirname(__FILE__), 'fastlane', 'Pluginfile')
eval_gemfile(plugins_path) if File.exist?(plugins_path)
