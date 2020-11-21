(ns redeemer-mobile.events
  (:require
    [re-frame.core :refer [reg-event-db reg-event-fx after reg-fx]]
    [clojure.spec.alpha :as s]
    [redeemer-mobile.db :as db :refer [app-db]]
    [redeemer-mobile.common.util :as util]
    [redeemer-mobile.common.props :as props]
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

(defn page-of-pages? [page]
  (contains? page :pages))

(defn make-remote-call [page]
  (let [current-page-key (keyword (str (:current-page-number page)))
        endpoint (if (page-of-pages? page)
                   (-> page :pages current-page-key :request-url)
                   (:request-url page))]
    (prn (str "Fetching from " endpoint "..."))
    (prn (str "for page" page))
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
    (let [{:keys [request-url]} page]
      (if (and (not (nil? request-url))
               (not (:requested page)))
        (do (println "Retrieving fresh data")
            (make-remote-call page))))))

;; -- Event Handlers --------------------------------------------------------------

(defn get-page
  ([{:keys [pages]} page-name]
   (get-page pages page-name 0))
  ([pages page-name page-of-page]
   (if (= 0 page-of-page)
     ((keyword page-name) pages)
     (let [page-name-key (keyword page-name)
           parent-page (page-name-key pages)
           page-number-key (keyword (str page-of-page))]
       (assoc-in parent-page [:pages page-name-key :pages page-number-key]
                 (-> pages page-name-key
                     :pages
                     page-number-key))))))

(reg-event-fx
  :option-pressed
  (fn [{:keys [db]} [_ nav-option]]
    {:db (assoc db :page (keyword nav-option)
                   :menu-state :closed)
     :fx [[:page-content-requested (get-page db nav-option)]]}))

(reg-event-db
  :page-content-received
  (fn [db [_ page-to-change content]]
    (let [current-page (:page db)
          current-page-number (:current-page-number page-to-change)
          current-page-number-key (keyword (str current-page-number))
          new-db (if (page-of-pages? page-to-change)
                   (-> db (assoc-in [:pages current-page :pages current-page-number-key :content] content)
                       (assoc-in [:pages current-page :pages current-page-number-key :requested] true))
                   (-> db (assoc-in [:pages current-page :content] content)
                       (assoc-in [:pages current-page :requested] true)))]
      new-db)))

;;TODO generalize
(reg-event-fx
  :blog-next-button-pressed
  (fn [{:keys [db]} [_ page-size page-number]]
    (let [next-page-number (+ 1 page-number)
          next-page-number-key (keyword (str next-page-number))
          new-db (-> db (assoc-in [:pages :Blog :current-page-number] next-page-number)
                     (assoc-in [:pages :Blog :page-size] page-size)
                     (assoc-in [:pages :Blog :pages next-page-number-key :content] ["Loading..."])
                     (assoc-in [:pages :Blog :pages next-page-number-key :request-url] (str (-> props/props :apis :ambassador-posts) next-page-number)))]
      {:db new-db
       :fx [[:page-content-requested
             (get-page (:pages new-db) "Blog" next-page-number)]]})))

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