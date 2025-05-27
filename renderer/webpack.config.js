const path = require('path');

module.exports = {
  entry: './src/client/main.ts',
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
    ],
  },
  resolve: {
    extensions: ['.tsx', '.ts', '.js'],
  },
  output: {
    filename: 'main.js',
    path: path.resolve(__dirname, 'dist/client'),
  },
  devServer: {
    static: {
      directory: path.join(__dirname, 'dist/client'),
    },
    compress: true,
    port: 3000,
    proxy: {
      '/api': 'http://localhost:3000',
    },
  },
}; 