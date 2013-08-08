(ns kawsc.system
  (:require [taoensso.timbre :as log]
            [kawsc.utils :as utils]
            [kawsc.server :as server]))

(defn- configure-logger! [config]
  (let [level (keyword (:level config))
        file-name (:output-file config)]
    (log/set-level! level)
    (log/set-config! [:appenders :spit :enabled?] true)
    (log/set-config! [:shared-appender-config :spit-filename] file-name)))

(defn system
  "Constructs system."
  ([] (system "etc/kawsc.cfg"))
  ([config-file]
     (let [config (utils/load-config config-file)]
       (configure-logger! (:log config))
       {:config config
        :aws-creds (:aws config)})))

(defn start
  "Performs side effects to initialize the system, acquire resources,
   and starts. Returns an updated version of the system."
  [system]
  (let [config (:config system)
        stop-http (server/start-server system (:server config))]
    (assoc system :stop-http stop-http)))

(defn stop
  "Performs side effects to shut down the system and release its
   resources. Returns an updated version of the system."
  [system]
  (when system
    (let [stop-http (:stop-http system)]
      (stop-http)
      (dissoc system :stop-http))))
