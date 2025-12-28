// Prevent webpack production minification (Terser) from stalling on Windows for large Kotlin/JS bundles.
// This keeps `jsBrowserProductionWebpack` terminating deterministically.

if (config.mode === 'production') {
    config.optimization = config.optimization || {};
    config.optimization.minimize = false;
}
