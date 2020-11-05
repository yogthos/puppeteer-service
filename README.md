## puppeteer service

The reporting service accepts a request in format of: `{:html "<html>Hello</html>" :options {}}`, where `:html` should contain the HTML document to be rendered, and the `:options` specifies Puppeteer options that the service should use to render the document. The HTML must embed CSS and Js associated with it.

### Building

Initialize NPM dependencies

    npm install

The service is built during development by running

    npx shadow-cljs watch app

The service is packaged by running

    npx shadow-cljs release app

Compiling the service will produce `puppeteer-service.js` that can be run with Node:

    node puppeteer-service.js

The service looks for `config.edn` file containing `:port` key, e,g: `{:port 3000}` or for a file pointed by the `PUPPETEER_SERVICE_CONFIG` environment variable.

The service can be tested using the `test-client.clj` Babashka script.