(ns om-next-e2e.ui
  (:require [goog.dom :as gdom]
            [cljs-http.client :as http]

            [cljs.core.async :refer [<! >! put! chan]]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defn send-post [path query cb]
  (let [req (http/post path {:transit-params query})]
    (go (cb (<! req)))))

(defn send-query [query cb]
  (send-post "/query" query cb))

(defn send-to-api [{:keys [api] :as remotes} cb]
  (send-query api (fn [{:keys [body status]}]
                    (when (= status 200)
                      (cb body)))))

(defonce app-state (atom {:count 0}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defmethod read :default [{:keys [state ast]} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value :api ast}
      :not-found)))

(defmethod mutate 'ui/increment [{:keys [state ast]} _ {:keys [value]}]
  {:value {:keys [:count]}
   :remote true
   :api ast
   :action (fn []
             (swap! state #(update % :count (fn [old] (+ old value)))))})

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (om/parser {:read read :mutate mutate})
     :send send-to-api
     :remotes [:api]}))

(defn mutate! [query]
  (om/transact! reconciler query))

(enable-console-print!)

(defui Counter
  static om/IQuery
  (query [this] [:count])
  Object
  (render [this]
          (dom/div nil
                   (dom/div nil (str "Counter: " (:count (om/props this))))
                   (dom/button #js {:onClick #(mutate! `[(ui/increment {:value 1}) :count])}
                               "Increment"))))

(om/add-root! reconciler
  Counter (gdom/getElement "main-app-area"))
