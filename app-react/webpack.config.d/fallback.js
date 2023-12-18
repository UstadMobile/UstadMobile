
// Required since Webpack 5 to explicitly specify fallback
;(function(config) {
    config.resolve.fallback = config.resolve.fallback || { };
    config.resolve.fallback["crypto"] =  require.resolve("crypto-browserify");
    config.resolve.fallback["stream"] = require.resolve("stream-browserify");
})(config);
