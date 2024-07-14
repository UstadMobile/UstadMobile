/*
 * Prevent webpack errors - we do not want to include polyfills for the below
 */
config.resolve.fallback = {
    "fs": false,
    "buffer": false,
    "path": false
};
