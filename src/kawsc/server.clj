(ns kawsc.server
  (:require [compojure.route :refer [resources]]
            [compojure.core :refer [routes GET]]
            [ring.util.response :as res]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [org.httpkit.server :refer [run-server]]
            [micro-middleware.json-support :as mmj]
            [cheshire.custom :as jc]
            [kawsc.aws :as aws]))

(defn- json-response
  ([payload] (json-response payload 200))
  ([payload status-code]
     (-> (res/response payload)
         (res/status status-code)
         (res/header "Content-Type" "application/json; charset=utf8"))))

(defn- create-routes [system]
  (routes
   (GET "/" [] (res/redirect "/index.html"))
   (GET "/instances" [] (json-response (aws/instances system)))
   (GET "/reservations" [] (json-response (aws/reservations system)))
   (resources "/")))

(defn start-server [system {:keys [listening-port]}]
  (jc/add-encoder org.joda.time.DateTime
                  #((println "WTF: encoding joda")
                    (jc/encode-date (java.util.Date. (.getMillis %1)) %2)))
  (run-server (-> (create-routes system)
                  (wrap-keyword-params)
                  (wrap-nested-params)
                  (wrap-params)
                  (wrap-session)
                  (mmj/wrap-json-params :hyphenize true)
                  (mmj/wrap-json-response :dehyphenize true))
              {:port listening-port}))
