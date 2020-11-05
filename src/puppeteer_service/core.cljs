(ns puppeteer-service.core
  (:require
   [cljs.reader :refer [read-string]]
   ["fs" :as fs]
   ["http" :as http]
   ["puppeteer" :as puppeteer]))

(defn js->edn [data]
  (js->clj data :keywordize-keys true))

(defn exit-with-error [error]
  (js/console.error error)
  (js/process.exit 1))


(defn find-config []
  (or (-> js/process .-env .-PUPPETEER_SERVICE_CONFIG)
      "config.edn"))

(defn read-edn [config]
  (if config
    (if (fs/existsSync config)
      (read-string (fs/readFileSync config "UTF-8"))
      (exit-with-error (str "config file does not exist: " config)))
    nil))

(defn render-page [page {:keys [html options]} handler error-handler]
  (try
    (.then page
           (fn [page _]             
             (-> (.setContent ^js page html)
                 (.then (fn [_ _]
                          (-> (.pdf ^js page (clj->js options))
                              (.then handler)
                              (.catch error-handler))))
                 (.catch error-handler))))
    (catch js/Error error
      (error-handler error))))

(defn listener [page]
  (fn [req res]
    (let [body (js/Array.)]
      (.on ^js req "data" #(.push body %))
      (.on ^js req "end"
           #(render-page page 
                         (read-string (.toString body))
                         (fn [data]                                                  
                           (.setHeader ^js res "Content-type" "application/pdf")
                           (.end ^js res data))
                         (fn [error]
                           (js/console.error (.-message error))
                           (set! (.-statusCode ^js res) 500)
                           (.setHeader ^js res "Content-type" "text/plain")
                           (.end ^js res (str "error handling request: " (.-message error)))))))))

(defn main []
  (let [config (read-edn (find-config))
        server (.createServer http 
                              (-> (.launch puppeteer (clj->js {:args ["--no-sandbox" "--disable-setuid-sandbox"]}))
                                  (.then (fn [browser _] (.newPage ^js browser)))
                                  (listener)))]
    (println "starting reporting service on port: " (:port config))
    (.listen server (:port config))))

