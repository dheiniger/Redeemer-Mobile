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

(defn make-remote-call [page endpoint]
  (prn (str "Fetching from " endpoint "..."))
  (go (let [response (<! (http/get endpoint))]
        (if (= 200 (:status response))
          (do
            (prn "response was successful: ")
            (re-frame.core/dispatch [:page-content-recieved page endpoint (:body response)]))
          (prn "An error has occured " response)))))

(reg-event-db
  :initialize-db
  validate-spec
  (fn [_ _]
    ;(make-remote-call "http://54.173.4.142/sites/12/posts/")
    (println "initializing...")
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
    (let [entries (last event)
          new-db (assoc db (util/make-content-keyword (second event)) entries)]
      new-db)))

(reg-event-db
  :blog-back-button-pressed
  (fn [db event]
    (let [page-size (second event)
          page-number (last event)
          new-db (assoc db :blog-post-page-number (if (> page-number 1) (- page-number 1)
                                                                        1)
                           :blog-post-page-size page-size)] ;;TODO might delete this]
      (println "blog back button pressed")
      (println "event is: " event)
      (println "new db is: " new-db)
      new-db)))

(reg-event-db
  :blog-next-button-pressed
  (fn [db event]
    db))

(reg-event-fx
  :menu-opened
  (fn [co-effects]
    (update-menu-state co-effects)))

(defn toggle-menu [menu-state]
  (if (= :open menu-state)
    :closed
    :open))

(defn update-menu-state [co-effects]
  (let [db (:db co-effects)
        menu-state (:menu-state db)
        new-db (assoc db :menu-state (toggle-menu menu-state))]
    (assoc new-db :menu-state :closed)
    {:db new-db}))

(reg-event-fx
  :menu-closed
  (fn [co-effects]
    (update-menu-state co-effects)))

;;TODO remove this
(defn make-remote-call-test [endpoint]
  (prn (str "Fetching from " endpoint "..."))
  (go (let [response (<! (http/get endpoint))]
        (if (= 200 (:status response))
          (do
            (prn "response was successful..."))
          ;;(re-frame.core/dispatch [:page-content-recieved page endpoint (:body response)]))
          (prn "An error has occured " response)))))

