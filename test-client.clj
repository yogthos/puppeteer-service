(ns test-client.core
  (:require 
   [clojure.string :as str]
   [babashka.curl :as curl]
   [clojure.java.io :as io]))

;; hiccup-like
(defn html [v]
  (cond (vector? v)
        (let [tag (first v)
              attrs (second v)
              attrs (when (map? attrs) attrs)
              elts (if attrs (nnext v) (next v))
              tag-name (name tag)]
          (format "<%s%s>%s</%s>\n" tag-name (html attrs) (html elts) tag-name))
        (map? v)
        (str/join ""
                  (map (fn [[k v]]
                         (format " %s=\"%s\"" (name k) v)) v))
        (seq? v)
        (str/join " " (map html v))
        :else (str v)))

(io/copy
 (:body (curl/get "http://localhost:3000" {:body (str {:html    (html [:html
                                                                       [:body
                                                                        [:h1 "Hello World!"]
                                                                        [:p "this is a test"]]])
                                                       :css     "p {color: blue} h1 {color: red}"
                                                       :options {}})
                                           :as   :stream}))
 (io/file "result.pdf"))


