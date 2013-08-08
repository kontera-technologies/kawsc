(defproject kawsc "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [http-kit "2.1.8"]
                 [ring/ring-core "1.2.0"]
                 [compojure "1.1.5"]
                 [stask/micro-middleware "0.0.12"]
                 [com.taoensso/timbre "2.5.0"]
                 [com.novemberain/monger "1.6.0"]
                 [amazonica "0.1.13"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]
                                  [org.clojure/tools.namespace "0.2.4"]]
                   :source-paths ["dev"]
                   :plugins [[lein-midje "3.1.1"]]}}
  :main kawsc.core)
