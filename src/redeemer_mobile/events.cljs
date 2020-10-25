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

;;TODO do these need to be -fx?
;;TODO stop doing side effects (instead cause them)

(reg-event-db
  :option-pressed
  (fn [db event]
    (let [url (last event)]
    (println "Menu option pressed")
    (println "Event is: " event)
    (println "Co-effects are: " db)
    (if (not (nil? url));;TODO BAD - SIDE EFFECT
      (re-frame.core/dispatch [:page-load-requested (second event) url]))
    (assoc db :page (second event)
              :menu-state :closed))))

;;TODO cache
(reg-event-db
  :page-load-requested
  (fn [db event]
    (let [url (last event)]
    (prn "Learn page requested...")
    (prn "co-effects are " db)
    (prn "event is " event)
    (prn "db is " (:db db))
    (let [db (assoc db :learn-page-content "Loading...")];;TODO generalize this
      (make-remote-call url)
      (prn "returning " db)
      db))))

(reg-event-db
  :learn-page-content-recieved
  (fn [db event]
    (print "learn page content received")
    (let [db (assoc db :learn-page-content (second event))]
      db)))

(reg-event-fx
  :menu-opened
  (fn [co-effects]
    (update-menu-state co-effects)))

(reg-event-fx
  :menu-closed
  (fn [co-effects]
    (update-menu-state co-effects)))

(defn update-menu-state [co-effects]
  (let [db (:db co-effects)
        menu-state (:menu-state db)
        new-db (assoc db :menu-state (toggle-menu menu-state))]
    (assoc new-db :menu-state :closed)
    {:db new-db}))

(defn toggle-menu [menu-state]
  (if (= :open menu-state)
    :closed
    :open))

(defn make-remote-call [endpoint]
  (prn (str "Fetching from " endpoint "..."))
  (go (let [response (<! (http/get endpoint))]
        (if (= 200 (:status response))
          (do
            (prn response)
            (re-frame.core/dispatch [:learn-page-content-recieved (:body response)]))
          (print "An error has occured " response)))))
