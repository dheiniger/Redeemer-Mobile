(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after reg-fx]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


;; -- Interceptors ------------------------------------------------------------
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


;; -- Event Handlers --------------------------------------------------------------

(reg-event-db
  :option-pressed
  (fn [db menu-text]
    (assoc db :page (keyword (second menu-text))
              :menu-state :closed)))

(reg-event-db
  :menu-opened
  (fn [db _]
    (assoc db :menu-state :open)))

(reg-event-db
  :menu-closed
  (fn [db _]
    (assoc db :menu-state :closed)))

(reg-event-db
  :initialize-db
  validate-spec
  (fn [_ _]
    (println "initializing...")
    app-db))