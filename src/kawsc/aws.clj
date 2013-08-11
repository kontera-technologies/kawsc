(ns kawsc.aws
  (:require [amazonica.core :refer []]
            [amazonica.aws.ec2 :refer :all]
            [clojure.string :as s]
            [clojure.core.memoize :as memo]))

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

(defn instances* [{:keys [aws-creds]}]
  (letfn [(tag-extractor [tag-name default i]
            (let [k (keyword (str "kona-" (s/lower-case tag-name)))
                  tag (first (filter #(.equalsIgnoreCase (:key %) tag-name)
                                     (:tags i)))
                  v (if tag (:value tag) default)]
              (assoc i k v)))
          (price-extractor [i]
            (assoc i :kona-price (get pricing (:instance-type i) -1)))
          (lifter [ks k default i]
            (assoc i k (get-in i ks default)))]
    (map (comp (partial tag-extractor "name" "unnamed")
               (partial tag-extractor "project" "unnamed")
               (partial tag-extractor "owner" "unknown")
               price-extractor
               (partial lifter
                        [:placement :availability-zone]
                        :kona-availability-zone
                        "unknown"))
         (flatten
          (map :instances
               (:reservations (describe-instances aws-creds)))))))

(def instances (memo/ttl instances* :ttl/threshold 120000))

(defn reservations* [{:keys [aws-creds] :as ctx}]
  (let [reserved-instances (:reserved-instances
                            (describe-reserved-instances aws-creds))
        running-instances (reduce #(let [key [(:kona-availability-zone %2)
                                              (:instance-type %2)]
                                         instances (get %1 key 0)]
                                     (assoc %1 key (inc instances)))
                                  {}
                                  (filter #(= (get-in % [:state :name]) "running")
                                          (instances ctx)))]
    (loop [result []
           input (:reserved-instances (describe-reserved-instances aws-creds))
           running-instances running-instances]
      (if (empty? input)
        result
        (let [r (first input)
              key [(:availability-zone r) (:instance-type r)]
              reserved-count (:instance-count r)
              running-count (get running-instances key 0)
              used-count (if (> reserved-count running-count)
                           (- reserved-count running-count)
                           reserved-count)
              running-count (max 0 (- running-count used-count))]
          (recur (conj result (assoc r :kona-used-instance-count used-count))
                 (rest input)
                 (assoc running-instances key running-count)))))))

(def reservations (memo/ttl reservations* :ttl/threshold 120000))
