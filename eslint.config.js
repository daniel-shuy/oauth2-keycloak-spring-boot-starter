const {includeIgnoreFile} = require('@eslint/compat');
const stylistic = require('@stylistic/eslint-plugin');
const eslintPluginJsonc = require('eslint-plugin-jsonc');
const path = require('node:path');

const gitignorePath = path.resolve(__dirname, '.gitignore');

module.exports = [
    includeIgnoreFile(gitignorePath),
    ...eslintPluginJsonc.configs['flat/recommended-with-json5'],
    {
        files: ['**/*.json5'],
        plugins: {
            '@stylistic': stylistic
        },
        ...stylistic.configs['recommended-flat'],
        rules: {
            'jsonc/array-bracket-newline': ['error', 'always'],
            'jsonc/array-element-newline': 'error',
            'jsonc/comma-dangle': ['error', 'always-multiline'],
            'jsonc/comma-style': 'error',
            'jsonc/indent': ['error', 2],
            'jsonc/key-spacing': 'error',
            'jsonc/no-irregular-whitespace': 'error',
            'jsonc/object-curly-newline': ['error', 'always'],
            'jsonc/object-property-newline': 'error',
            'jsonc/quotes': ['error', 'double'],
            'multiline-comment-style': ['error', 'bare-block'],
        }
    },
];
