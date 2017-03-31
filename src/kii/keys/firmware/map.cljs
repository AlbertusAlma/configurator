(ns kii.keys.firmware.map
  (:refer-clojure :exclude [keys])
  (:require [kii.keys.firmware.predefined :as predefined]))

(defn ->category
  [name label]
  {:name name
   :label label})

(def categories
  (map
    #(apply ->category %)
    [[:spec "special"]
     [:std "standard"]
     [:core "core"]
     [:mmed "multimedia"]
     [:num "numpad"]
     [:i11l "international"]
     [:mac "macros"]
     ]))

(defn ->key
  [order name label aliases group]
  {:name name
   :aliases (or aliases [label])
   :label label
   :group group
   :order order})

(defonce keys
  (into
    {}
    (map-indexed
      #(vector (first %2) (apply ->key % %2))
      predefined/all
      )))

(defn alias->key
  [alias]
  (some (fn [[name k]]
          (let [aliases (:aliases k)]
            (and (some #{alias} aliases)
                 k)))
        keys))