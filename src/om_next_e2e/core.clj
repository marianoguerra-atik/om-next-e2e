(ns om-next-e2e.core
  (:require
    [bidi.ring :refer [make-handler]]
    [clojure.tools.logging :as log]
    [immutant.web :as web])
  (:gen-class))

(defn handle-action [{:keys [body]}]
  {:status 200 :body (str "action: " body)})

(defn handle-query [{:keys [body]}]
  {:status 200 :body (str "query action: " body)})

(defn handle-not-found [_]
  {:status 404 :body "Not Found"})

(defn wrap-handler [handler has-body]
  (fn [{:keys [body]}]
    (let [req-body (when has-body (slurp body))]
      (try
        (handler {:body req-body})
        (catch Throwable ex
          (log/error ex "Error calling handler")
          {:status 500 :body "Internal Error"})))))

(def req-handlers {:action      (wrap-handler handle-action true)
                   :query       (wrap-handler handle-query true)
                   :not-found   (wrap-handler handle-not-found false)})

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
