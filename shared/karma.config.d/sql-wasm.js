// Configure karma to serve the WASM file
// Webpack copies sql-wasm.wasm, and we need to make it available at /sql-wasm.wasm
const path = require('path');
const fs = require('fs');

// Add custom middleware to serve the WASM file
config.beforeMiddleware = config.beforeMiddleware || [];
config.beforeMiddleware.push('sql-wasm-middleware');

config.plugins = config.plugins || [];
config.plugins.push({
  'middleware:sql-wasm-middleware': ['factory', function() {
    return function(req, res, next) {
      if (req.url === '/sql-wasm.wasm') {
        const wasmPath = path.join(__dirname, '../../node_modules/sql.js/dist/sql-wasm.wasm');
        if (fs.existsSync(wasmPath)) {
          res.writeHead(200, {'Content-Type': 'application/wasm'});
          fs.createReadStream(wasmPath).pipe(res);
          return;
        }
      }
      next();
    };
  }]
});
