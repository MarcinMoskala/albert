const webpack = require('webpack');

// Inject SERVER_URL from environment into the browser bundle so globalThis.SERVER_URL is available
config.plugins.push(
    new webpack.DefinePlugin({
        'globalThis.SERVER_URL': JSON.stringify(process.env.SERVER_URL || ''),
    })
);

// Ensure assets are resolved relative to the worker/bundle root
config.output = config.output || {};
config.output.publicPath = '/app/';
