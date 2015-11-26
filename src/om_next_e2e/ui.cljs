(ns om-next-e2e.ui
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(defonce app-state (atom {:counter 0}))

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defn get-state-key [{:keys [state]} key]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      :not-found)))

(defmethod read :default [env key params]
  (get-state-key env key))

(defmethod mutate 'ui/increment [{:keys [state]} _ {:keys [value]}]
  {:value {:keys [:counter]}
   :action (fn [] (swap! state
                         #(update % :counter (fn [old] (+ old value)))))})

(def reconciler
  (om/reconciler
    {:state app-state :parser (om/parser {:read read :mutate mutate})}))

(defn mutate! [query]
  (om/transact! reconciler query))

(enable-console-print!)

(defui Counter
  static om/IQuery
  (query [this] [:counter])
  Object
  (render [this]
          (dom/div nil
                   (dom/div nil (str "Counter: " (:counter (om/props this))))
                   (dom/button #js {:onClick
                                    #(mutate!
                                       `[(ui/increment {:value 1}) :counter])}
                               "Increment"))))

(om/add-root! reconciler
  Counter (gdom/getElement "main-app-area"))
