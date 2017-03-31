(ns kii.ui.conf.keyboard.handlers
  (:require [re-frame.core :as rf]
            [kii.keys.firmware.map :as fw]
            [kii.keys.core :as keys]
            [kii.ui.conf.keyboard.subscriptions :as sub]
            [kii.ui.conf.subscriptions :as conf-sub]
            [kii.ui.conf.layer-select.subscriptions :as ls-sub]))


(defn set-selected-key
  [db [_ value]]
  (assoc-in db [:conf :selected-key] value))

(rf/reg-event-db :set-selected-key set-selected-key)

;; TODO: Move keypress logic to emit this event
(defn update-selected-key
  [db [_ new-key]]
  db
  #_(let []
      (-> db
          (assoc-in [:conf :kll :matrix] new-matrix)
          (assoc-in [:conf :selected-key] new-key)))
  )

(rf/reg-event-db :update-selected-key update-selected-key)

;; TODO: Keypress pipline
;;  Most easily accomplished by simply adding items to a "pressed"
;;  array and then waiting to fire an action until after the last
;;  key is released or a timeout occurs.
;;  This would be important for keys that are not 1:1 mappings in
;;  some layouts (not as important if we have an on-screen keyboard.
(defn handle-keyup
  [db [_ value]]
  db)

(defn handle-keypress
  [db [_ value]]
  db)

(defn get-iec9995
  [key-event]
  (let [e (:event key-event)
        loc (:location e)
        key-code (:key-code key-event)
        adj-key-code (+ (* 1000 loc) key-code)
        key (get (keys/code->iec) key-code)
        adj-key (get (keys/code->iec) adj-key-code)]
    ;;(print "Key Down - " key-code)
    ;;(print "Adjusted Key Code - " adj-key-code)
    ;;(print "Key " key)
    ;;(print "Adj Key" adj-key)
    (or adj-key key)))

(defn handle-keydown
  [db [_ value]]
  (if-let [selected-key (sub/get-selected-key db nil)]
    (let [iec9995-loc (get-iec9995 value)
          mapped (get (keys/iec->key) iec9995-loc)
          predef (get fw/keys (:key mapped))
          active-layer (ls-sub/get-active-layer db nil)
          matrix (conf-sub/get-matrix db nil)
          new-key (assoc-in
                    selected-key
                    [:layers (keyword (str active-layer))]
                    (keys/merge predef mapped))
          new-matrix (->> matrix
                          (map
                            (fn [key]
                              (if (= key selected-key)
                                new-key
                                key)))
                          vec)]
      ;;(print "mapped -> " mapped)
      ;;(print "predef -> " predef)
      ;; TODO: Replace with emitting two events:
      ;;       1. Set Key
      ;;       2. Set Selected Key
      (-> db
          (assoc-in [:conf :kll :matrix] new-matrix)
          (assoc-in [:conf :selected-key] new-key)))
    db))

(rf/reg-event-db :window/keyup handle-keyup)
(rf/reg-event-db :window/keydown handle-keydown)
(rf/reg-event-db :window/keypress handle-keypress)