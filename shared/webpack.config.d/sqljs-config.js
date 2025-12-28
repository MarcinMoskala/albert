// Configuration for SQL.js WebAssembly binary
// See: https://sqldelight.github.io/sqldelight/2.0.2/js_sqlite/sqljs_worker/

config.resolve = {
    fallback: {
        fs: false,
        path: false,
        crypto: false,
    }
};

const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');
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
config.output = config.output || {};
config.output.publicPath = '/app/';
