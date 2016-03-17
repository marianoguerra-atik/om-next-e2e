(defproject om-next-e2e "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main om-next-e2e.core
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.omcljs/om "1.0.0-alpha30"]
                 [cljs-http "0.1.39"]

                 [bidi "2.0.4"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.immutant/web "2.1.3"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.0-6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main       "om-next-e2e.ui"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/om-next-e2e.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true }}
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:main       "om-next-e2e.ui"
                                   :asset-path "js/compiled/out"
                                   :output-to  "resources/public/js/compiled/om-next-e2e.js"
                                   :optimizations :simple}}]}

  :figwheel { :css-dirs ["resources/public/css"] })
