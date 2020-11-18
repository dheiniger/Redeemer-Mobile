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

(defn make-remote-call [page]
  (let [endpoint (:request-url page)]
    (prn (str "Fetching from " endpoint "..."))
    (go (let [response (<! (http/get endpoint))]
          (if (= 200 (:status response))
            (do
              (prn "response was successful: ")
              (print "requested data for page " page)
              (re-frame.core/dispatch [:page-content-received page (:body response)]))
            (prn "An error has occured " response))))))

;;TODO still needed?
(defn get-page [db page-name]
  ((keyword page-name) (:pages db)))


(defn request-page-content [page]
  (let [request-url (:request-url page)]
    (if (and (not (nil? request-url))
             (= "Loading..." (:content page)))
      (do (println "Retrieving fresh data")
          (make-remote-call page))
      (:content page))))

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
    (let [page (get-page db (second event))
          request-url (:request-url page)]
      (if (not (nil? request-url))
        (re-frame.core/dispatch [:page-load-requested page])) ;;TODO BAD - SIDE EFFECT
      (assoc db :page (keyword (second event))
                :menu-state :closed))))

(reg-event-db
  :page-load-requested
  (fn [db event]
    (request-page-content (second event))                   ;;TODO big 'ol side-effect
    db))

(reg-event-db
  :page-content-received
  (fn [db event]
    (println "Page Content Received.  Event is: " event)
    (println "DB is: " db)
    (let [content (last event)
          pages (:pages db)
          page-to-change (second event)
          changed-page (assoc page-to-change :content (last event))
          page-name (:navigation (second event))
          k (keyword page-name)
          new-db (assoc-in db [:pages k :content] content)]
      (println "Pages are: " pages)
      (println "k is: " k)
      (println "new-db is: " new-db)
      (println "Page to change is: " page-to-change)
      (println "Changed-page is: " changed-page)
      new-db)))

(reg-event-db
  :blog-back-button-pressed
  (fn [db event]
    (let [page-size (second event)
          page-number (last event)                          ;;TODO move dec logic here
          new-db (assoc db :blog-post-page-number (if (> page-number 1) (- page-number 1) 1)
                           :blog-post-page-size page-size)]
      (make-remote-call nil)                                ;;TODO
      new-db)))

;;TODO condense these into 1 function?
(reg-event-db
  :blog-next-button-pressed
  (fn [db event]
    (let [page-size (second event)
          page-number (last event)                          ;;TODO move inc logic here
          new-db (assoc db :blog-post-page-number (+ page-number 1)
                           :blog-post-page-size page-size)]
      (make-remote-call nil)                                ;;TODO
      new-db)))

(reg-event-fx
  :menu-opened
  (fn [co-effects]
    (update-menu-state co-effects)))

(reg-event-fx
  :menu-closed
  (fn [co-effects]
    (update-menu-state co-effects)))