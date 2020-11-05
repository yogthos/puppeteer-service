(ns report-service-test.core
  (:require 
   [babashka.curl :as curl]
   [clojure.java.io :as io]))

(io/copy
 (:body (curl/get "http://localhost:3000" {:body (str {:html "<html>Hello World</html>"
                                                       :options {}}) :as :stream}))
 (io/file "result.pdf"))

