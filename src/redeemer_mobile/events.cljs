(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]]
    [redeemer-mobile.common.util :as util]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [clojure.string :as str])
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
      (if (not (nil? url))                                  ;;TODO BAD - SIDE EFFECT
        (re-frame.core/dispatch [:page-load-requested (second event) url]))
      (assoc db :page (second event)
                :menu-state :closed))))

(reg-event-db
  :page-load-requested
  (fn [db event]
    (let [url (last event)
          page (second event)
          page-content-key (util/make-content-keyword page)]
      ;;TODO is this bad?
      (if (nil? (page-content-key db))
        (do
          (make-remote-call page url)
          (assoc db page-content-key "Loading..."))
        db))))

(reg-event-db
  :page-content-recieved
  (fn [db event]
    (let [new-db (assoc db (util/make-content-keyword (second event)) (last (remove-html event)))]
      new-db)))

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

(defn make-remote-call [page endpoint]
  (prn (str "Fetching from " endpoint "..."))
  (go (let [response (<! (http/get endpoint))]
        (if (= 200 (:status response))
          (do
            (prn "response was successful")
            (re-frame.core/dispatch [:page-content-recieved page endpoint (:body response)]))
          (prn "An error has occured " response)))))

(defn remove-html [page-content]
  page-content )
