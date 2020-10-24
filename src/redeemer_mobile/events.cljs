(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;;TODO - for testing
(def JSON "{\n  \"firstName\": \"Daniel\",\n  \"lastName\": \"Heiniger\",\n  \"Something Nested\": {\n    \"Key 1\": \"Value 1\"\n  }\n}")
(def PARSED_JSON (js->clj (.parse js/JSON JSON) :keywordize-keys true))
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

;;TODO make api calls here??
;;TODO this might make the initialization really slow though so maybe move some to
;;TODO on-demand places, but cache it??

(reg-event-db
  :initialize-db
  validate-spec
  (fn [_ _]
    ;;(make-remote-call "http://54.144.99.209:8085/") TODO
    (println "initializing...")
    (println "Json: " PARSED_JSON)
    (println "First name is: " (:firstName PARSED_JSON))
    (println "Type is: " (type PARSED_JSON))
    app-db))

(reg-event-db
  :set-content
  validate-spec
  (fn [db [_ value]]
    (assoc db :content value)))
;;TODO do these need to be -fx?

(reg-event-db
  :option-pressed
  (fn [coeffects event]
    (let [db (:db coeffects)]
      (assoc db :content (second event) :menu-state "closed"))))

(reg-event-fx
  :menu-opened
  (fn [coeefects]
    (update-menu-state coeefects)))

(reg-event-fx
  :menu-closed
  (fn [coeefects]
    (update-menu-state coeefects)))

(defn update-menu-state [coeefects]
  (let [db (:db coeefects)
        menu-state (:menu-state db)
        new-db (assoc db :menu-state (toggle-menu menu-state))]
    (assoc new-db :menu-state "closed")
    {:db new-db}))

(defn toggle-menu [menu-state]
  (if (= "open" menu-state)
    "closed"
    "open"))

(defn make-remote-call [endpoint]
  (go (println "fetching data...")
      (prn (<! (http/get endpoint)))
      :body))