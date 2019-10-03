
const webpack = require("webpack");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "../data/public");

module.exports = {
    entry: './build/kotlin-js-min/main/js.js',
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
            path.resolve(__dirname, "build/kotlin-js-min/main")
        ]
    },
    devtool: 'source-map',
    plugins: [
        new UglifyJSPlugin({
            sourceMap: true
        })
    ]
};
