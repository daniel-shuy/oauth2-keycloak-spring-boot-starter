const eslintPluginJsonc = require("eslint-plugin-jsonc");

module.exports = [
  ...eslintPluginJsonc.configs["flat/recommended-with-json5"],
  {
    files: ["**/*.json5"],
    rules: {
      "jsonc/array-bracket-newline": ["error", "always"],
      "jsonc/array-element-newline": "error",
      "jsonc/comma-dangle": ["error", "always-multiline"],
      "jsonc/comma-style": "error",
      "jsonc/indent": ["error", 2],
      "jsonc/key-spacing": "error",
      "jsonc/no-irregular-whitespace": "error",
      "jsonc/object-curly-newline": ["error", "always"],
      "jsonc/object-property-newline": "error",
      "jsonc/quotes": ["error", "double"],
      "multiline-comment-style": ["error", "bare-block"],
    },
  },
];
