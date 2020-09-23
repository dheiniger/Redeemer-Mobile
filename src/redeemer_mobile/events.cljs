(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
  :initialize-db
  validate-spec
  (fn [_ _]
    app-db))

(reg-event-db
  :set-greeting
  validate-spec
  (fn [db [_ value]]
    (assoc db :greeting value)))

(reg-event-fx
  :menu-opened
  (fn [coeefects event]
    (update-menu-state coeefects event)))

(reg-event-fx
  :menu-closed
  (fn [coeefects event]
    (update-menu-state coeefects event)))

(defn update-menu-state [coeefects event]
  (let [db (:db coeefects)
        menu-state (:menu-state db)
        new-db (assoc db :menu-state (toggle-menu menu-state))]
    (print "in toggle-menu")
    (print (str "co-effects are " coeefects))
    (print (str "event is " event))
    (print (str "db is: " db))
    (assoc new-db :menu-state "closed")
    (print (str "new db is: " new-db))
    {:db new-db}))

(defn toggle-menu [menu-state]
  (if (= "open" menu-state)
    "closed"
    "open"))
