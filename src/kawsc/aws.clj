(ns kawsc.aws
  (:require [amazonica.core :refer []]
            [amazonica.aws.ec2 :refer :all]))

(defn instances [{:keys [aws-creds]}]
  (describe-instances aws-creds))
