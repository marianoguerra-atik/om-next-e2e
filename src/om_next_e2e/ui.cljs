(ns om-next-e2e.ui
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]))

(enable-console-print!)

(println "Hello world!")

(defui HelloWorld
  Object
  (render [this]
    (dom/div nil "Hello, World!")))

(def hello (om/factory HelloWorld))

(js/ReactDOM.render (hello) (gdom/getElement "main-app-area"))
