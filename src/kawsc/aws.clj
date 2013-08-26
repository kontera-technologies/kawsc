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

;; 1-year, light, US East
(def ^:private ^:const reserved-pricing
  {"m1.small" 24.48
   "m1.medium" 48.96
   "m1.large" 97.92
   "m1.xlarge" 195.12
   "m3.xlarge" 215.28
   "m3.2xlarge" 430.56
   "t1.micro" 8.64
   "m2.xlarge" 121.68
   "m2.2xlarge" 243.36
   "m2.4xlarge" 486.72
   "c1.medium" 64.8
   "c1.xlarge" 259.2
   "cc1.4xlarge" 534.24 ;; not available anymore, this is the old price
   "cc1.8xlarge" 650.88
   "cr1.8xlarge" 1108.8
   "cg1.4xlarge" 1512 ;; not availabie, using regular price
   "hi1.4xlarge" 1063.44
   "hs1.8xlarge" 1612.8})

(defn instances-list* [aws-creds]
  (flatten (map :instances (:reservations (describe-instances aws-creds)))))
(def instances-list (memo/ttl instances-list* :ttl/threshold 120000))

(defn reserved-instances-list* [aws-creds]
  (:reserved-instances (describe-reserved-instances aws-creds)))
(def reserved-instances-list (memo/ttl reserved-instances-list* :ttl/threshold 120000))

(defn instances [{:keys [aws-creds]}]
  (let [reserved-instances (reduce (fn [acc r]
                                     (let [k [(:availability-zone r)
                                              (:instance-type r)]
                                           v (get acc k 0)]
                                       (assoc acc k (+ v (:instance-count r)))))
                                   {}
                                   (reserved-instances-list aws-creds))]
    (letfn [(tag-extractor [tag-name default i]
              (let [k (keyword (str "kona-" (s/lower-case tag-name)))
                    tag (first (filter #(.equalsIgnoreCase (:key %) tag-name)
                                       (:tags i)))
                    v (if tag (:value tag) default)]
                (assoc i k v)))
            (lifter [ks k default i]
              (assoc i k (get-in i ks default)))]
      (loop [result []
             input (map (comp (partial tag-extractor "name" "unnamed")
                        (partial tag-extractor "project" "unnamed")
                        (partial tag-extractor "owner" "unknown")
                        (partial lifter [:placement :availability-zone]
                                 :kona-availability-zone
                                 "unknown"))
                  (instances-list aws-creds))
             reserved-instances reserved-instances]
        (if (empty? input)
          result
          (let [i (first input)
                k [(:kona-availability-zone i)
                   (:instance-type i)]
                reserved-count (get reserved-instances k 0)
                running? (= (get-in i [:state :name]) "running")
                spot? (= (:instance-lifecycle i) "spot")
                [reserved price reserved-count] (if (and running?
                                                         (not spot?)
                                                         (> reserved-count 0))
                                                  [true
                                                   (get reserved-pricing
                                                        (:instance-type i))
                                                   (dec reserved-count)]
                                                  [false
                                                   (get pricing
                                                        (:instance-type i))
                                                   reserved-count])]
            (recur (conj result (-> i
                                    (assoc :kona-reserved reserved)
                                    (assoc :kona-price price)))
                   (rest input)
                   (assoc reserved-instances k reserved-count))))))))

(defn reservations [{:keys [aws-creds]}]
  (let [reserved-instances (reserved-instances-list aws-creds)
        running-instances (reduce #(let [k [(:kona-availability-zone %2)
                                            (:instance-type %2)]
                                         v (get %1 k 0)]
                                     (assoc %1 k (inc v)))
                                  {}
                                  (filter #(and (= (get-in % [:state :name]) "running")
                                                (not= (:instance-lifecycle %) "spot"))
                                          (instances-list aws-creds)))]
    (loop [result []
           input reserved-instances
           running-instances running-instances]
      (if (empty? input)
        result
        (let [r (first input)
              k [(:availability-zone r)
                 (:instance-type r)]
              reserved-count (:instance-count r)
              running-count (get running-instances k 0)
              used-count (if (> reserved-count running-count)
                           (- reserved-count running-count)
                           reserved-count)
              running-count (max 0 (- running-count used-count))]
          (recur (conj result (assoc r :kona-used-instance-count used-count))
                 (rest input)
                 (assoc running-instances k running-count)))))))
