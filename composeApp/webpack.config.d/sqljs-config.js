// Configuration for SQL.js WebAssembly binary
// See: https://sqldelight.github.io/sqldelight/2.0.2/js_sqlite/sqljs_worker/

config.resolve = {
    fallback: {
        fs: false,
        path: false,
        crypto: false,
    }
};

const CopyWebpackPlugin = require('copy-webpack-plugin');
const path = require('path');
config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: require.resolve('sql.js/dist/sql-wasm.wasm'),
                to: '[name][ext]'
            }
        ]
    })
);

// Ensure assets are resolved relative to the worker/bundle root
config.output = config.output || {};
config.output.publicPath = '/app/';
