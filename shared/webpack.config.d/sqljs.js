// Fallbacks for node built-ins and copy WASM for sql.js worker
config.resolve = {
  ...(config.resolve || {}),
  fallback: {
    ...(config.resolve?.fallback || {}),
    fs: false,
    path: require("path-browserify"),
    crypto: require("crypto-browserify")
  }
};

const CopyWebpackPlugin = require("copy-webpack-plugin");
config.plugins = [
  ...(config.plugins || []),
  new CopyWebpackPlugin({
    patterns: [
      {
        from: require.resolve("sql.js/dist/sql-wasm.wasm"),
        to: "sql-wasm.wasm"
      }
    ]
  })
];