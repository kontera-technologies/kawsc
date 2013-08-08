(ns kawsc.utils
  (:require [cheshire.core :as json]))

(defn load-config [file-name]
  (json/parse-string (slurp file-name) true))
