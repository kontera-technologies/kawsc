(ns kawsc.aws
  (:require [amazonica.core :refer []]
            [amazonica.aws.ec2 :refer :all]
            [clojure.string :as s]))

(def ^:private ^:const pricing
  {"m1.small" 43.2
   "m1.medium" 86.4
   "m1.large" 172.8
   "m1.xlarge" 345.6
   "m3.xlarge" 360
   "m3.2xlarge" 720
   "t1.micro" 14.4
   "m2.xlarge" 295.2
   "m2.2xlarge" 590.4
   "m2.4xlarge" 1180.8
   "c1.medium" 104.4
   "c1.xlarge" 417.6
   "cc1.4xlarge" 936
   "cc1.8xlarge" 1728
   "cr1.8xlarge" 2520
   "cg1.4xlarge" 1512
   "hi1.4xlarge" 2232
   "hs1.8xlarge" 3312})

(defn instances [{:keys [aws-creds]}]
  (letfn [(tag-extractor [tag-name default i]
            (let [k (keyword (str "kona-" (s/lower-case tag-name)))
                  tag (first (filter #(.equalsIgnoreCase (:key %) tag-name)
                                     (:tags i)))
                  v (if tag (:value tag) default)]
              (assoc i k v)))
          (price-extractor [i]
            (assoc i :kona-price (get pricing (:instance-type i) -1)))]
    (map (comp (partial tag-extractor "name" "unnamed")
               (partial tag-extractor "project" "unnamed")
               (partial tag-extractor "owner" "unknown")
               price-extractor)
         (flatten
          (map :instances
               (:reservations (describe-instances aws-creds)))))))
