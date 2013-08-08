(ns user
  "Namespace to support hacking at the REPL."
  (:require [clojure.repl :refer [doc find-doc source]]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.pprint :refer [pprint]]
            [clojure.stacktrace :refer :all]
            [midje.repl :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]
            [kawsc.system :as sys]))

(def system nil)

(defn init
  "Constructs the current development system."
  []
  (alter-var-root #'system (constantly (sys/system))))

(defn start
  "Starts the current development system."
  []
  (alter-var-root #'system #'sys/start))

(defn stop
  "Shits down the current development system."
  []
  (alter-var-root #'system (fn [s] (when s (sys/stop s)))))

(defn go
  "Initializes the current development system and starts it."
  []
  (init)
  (start))

(defn reset
  "Stops, reloads, and starts current development system."
  []
  (stop)
  (refresh :after 'user/go))
