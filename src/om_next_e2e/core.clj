(ns om-next-e2e.core
  (:require
    [clojure.tools.logging :as log]
    [immutant.web :as web])
  (:gen-class))

(defn app [request]
    {:status 200
        :body "Hello world!"})

(defn -main []
  (let [host "localhost"
        port 8080
        path "/"]
  (log/info "Starting Server at" (str host ":" port path))
  (web/run app {:host host :port port :path path})))
