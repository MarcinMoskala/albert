// Ensure assets are resolved relative to the worker/bundle root
config.output = config.output || {};
config.output.publicPath = '/app/';

// Allow direct navigation / refresh on deep links to return index.html
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = {
    index: '/app/index.html'
};
