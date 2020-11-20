(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after reg-fx]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]]
    [redeemer-mobile.common.util :as util]
    [clojure.string :as str]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]])
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


;; -- Effect Handlers --------------------------------------------------------------

(defn make-remote-call [page]
  (let [{endpoint :request-url} page]
    (prn (str "Fetching from " endpoint "..."))
    (go (let [response (<! (http/get endpoint))]
          (if (= 200 (:status response))
            (do
              (prn "response was successful: ")
              (print "requested data for page " page)
              (re-frame.core/dispatch [:page-content-received page (:body response)]))
            (prn "An error has occured " response))))))

(reg-fx
  :page-content-requested
  (fn [page]
    (let [{:keys [request-url content]} page]
      (if (and (not (nil? request-url))
               (= "Loading..." content))
        (do (println "Retrieving fresh data")
            (make-remote-call page))))))

;; -- Event Handlers --------------------------------------------------------------

(defn get-page [{:keys [pages]} page-name]
  ((keyword page-name) pages))

(reg-event-fx
  :option-pressed
  (fn [{:keys [db]} [_ nav-option]]
    {:db       (assoc db :page (keyword nav-option)
                         :menu-state :closed)
     :fx [[:page-content-requested (get-page db nav-option)]]}))

(reg-event-db
  :page-content-received
  (fn [db [_ page-to-change content]]
    (let [k (keyword (:navigation page-to-change))
          new-db (assoc-in db [:pages k :content] content)]
      new-db)))

;(reg-event-db
;  :blog-back-button-pressed
;  (fn [db [_ page-size page-number]]
;    (let [new-db (assoc db :blog-post-page-number (if (> page-number 1) (- page-number 1) 1)
;                           :blog-post-page-size page-size)]
;      ;page-number (last event)                          ;;TODO move dec logic here
;
;      (make-remote-call nil)                                ;;TODO
;      new-db)))
;
;;;TODO condense these into 1 function?
;(reg-event-db
;  :blog-next-button-pressed
;  (fn [db [_ page-size page-number]]
;    (let [;;TODO move inc logic here
;          new-db (assoc db :blog-post-page-number (+ page-number 1)
;                           :blog-post-page-size page-size)]
;      (make-remote-call nil)                                ;;TODO
;      new-db)))

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