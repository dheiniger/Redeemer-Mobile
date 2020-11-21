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
          page-content-key (util/make-content-keyword page)]
      (page-content-key db))))

(reg-sub
  :get-blog-posts-page
  (fn [db _]
    (let [{{:keys [Blog]} :pages} db
          current-page-number (:current-page-number Blog)
          index-key (keyword (str current-page-number))]
      (assoc {} :page-number current-page-number
                :content (-> Blog
                             :pages
                             index-key
                             :content)))))
