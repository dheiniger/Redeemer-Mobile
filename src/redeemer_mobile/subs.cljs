(ns redeemer-mobile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [redeemer-mobile.common.util :as util]))

(reg-sub
  :get-page
  (fn [db _]
    (:page db)))

(reg-sub
  :get-menu-state
  (fn [db _]
    (:menu-state db)))

(reg-sub
  :get-page-content
  (fn [db _]
    (let [page (:page db)
          page-content-key (util/make-content-keyword page)
          to-return (page-content-key db)]                  ;;TODO don't make this a variable
      to-return)))

(reg-sub
  :get-blog-posts-page
  (fn [db _]
    (println "in get blog posts page")
    (println "db is " db)
    (let [blog-page-number (or (:blog-post-page-number db) 1)
          blog-post-page-size (or (:blog-post-page-size db) 10) ;;TODO clean this up
          current-blog-page-content  (:content(:Blog(:pages db)))
          toReturn (assoc {} :page-number (:blog-post-page-number db);;TODO don't make this a variable
                             :page-size (:blog-post-page-size db)
                             :content current-blog-page-content)]
    (println "Returning.... " toReturn)
    toReturn)))
