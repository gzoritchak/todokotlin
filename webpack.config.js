
// const webpack = require("webpack");
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "./data/public");

module.exports = {
    entry: 'todokotlin.js',
    output: {
        filename: "[name].bundle.js",
        path: dist,
        publicPath: ""
    },
    devServer: {
        contentBase: dist
    },
    module: {
    },
    resolve: {
        modules: [
            path.resolve(__dirname, "data/public")
        ]
    },
    devtool: 'source-map',
    plugins: [
        new UglifyJSPlugin({
            sourceMap: true
        })
	]
};
