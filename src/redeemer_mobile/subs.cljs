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
          to-return (page-content-key db)];;TODO
      to-return)))

(reg-sub
  :get-blog-posts-page
  (fn [db _]
    (println "in get blog posts page")
    (assoc {} :page-number (:blog-post-page-number db)
              :page-size (:blog-post-page-size db))))
