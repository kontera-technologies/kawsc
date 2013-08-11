(ns kawsc.core
  (:require [kawsc.system :as sys]))


(defn -main []
  (let [system (sys/system)]
    (sys/start system)))
