(ns om-next-e2e.core
  (:require
    [om-next-e2e.handlers :as handlers]
    [bidi.ring :refer [make-handler]]
    [cognitect.transit :as transit]
    [clojure.tools.logging :as log]
    [immutant.web :as web])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream])
  (:gen-class))

(defn transit-to-string [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))

(defn parse-transit [data]
  (let [reader (transit/reader data :json)]
    (transit/read reader)))

(defn response-to-transit [{:keys [body headers] :as response}]
  (assoc response
         :body (transit-to-string body)
         :headers (assoc headers "Content-Type" "application/transit+json")))

(defn call-handler [handler has-body body]
  (let [req-body (when has-body (parse-transit body))]
    (try
      (handler {:body req-body})
      (catch Throwable ex
        (log/error ex "Error calling handler")
        {:status 500 :body {:error "Internal Error"}}))))

(defn wrap-handler [handler has-body]
  (fn [{:keys [body]}]
    (response-to-transit (call-handler handler has-body body))))

(def req-handlers {:action      (wrap-handler handlers/action true)
                   :query       (wrap-handler handlers/query true)
                   :not-found   (wrap-handler handlers/not-found false)})

(def routes ["/" {"action" {:post :action}
                  "query" {:post :query}
                  true :not-found}])

(def app (make-handler routes req-handlers))

(defn -main []
  (let [host "localhost"
        port 8080
        path "/"]
  (log/info "Starting Server at" (str host ":" port path))
  (web/run app {:host host :port port :path path})))
