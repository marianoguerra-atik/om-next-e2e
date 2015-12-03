(ns om-next-e2e.handlers
  (:require 
    [clojure.tools.logging :as log]
    [om.next.server :as om]))

(def app-state (atom {:count 0}))

(defn dispatch [_ key _] key)

(defmulti readf dispatch)
(defmulti mutatef dispatch)

(def parser (om/parser {:read readf :mutate mutatef}))

(defn query [{:keys [body]}]
  (let [query-env {:state app-state}
        params body
        _ (log/info "query" params)
        r (parser query-env params)]
    (log/info "<<" r)
    {:status 200 :body r}))

(defn not-found [_]
  {:status 404 :body {:error "Not Found"}})

(defn get-state-key [{:keys [state]} key]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      :not-found)))

(defmethod readf :default [env key params]
  :not-found)

(defmethod readf :count [env key _]
  (get-state-key env key))

(defmethod mutatef 'ui/increment [{:keys [state]} _ {:keys [value]}]
  (swap! state
         #(update % :count (fn [old] (+ old value))))
  {:status 200
   :body {:value {:keys [:count]}}})

(defmethod mutatef :default [_ key params]
  {:status 404 :body {:error "Not Found" :key key :params params}})
