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
    (let [{:keys [blog-post-page-number blog-post-page-size]
           :or   {blog-post-page-number 1,
                  blog-post-page-size   10}} db
          current-blog-page-content (-> db :pages :Blog :content)]
      (prn "get blog posts page content is " current-blog-page-content)
      (assoc {} :page-number blog-post-page-number
                :page-size blog-post-page-size
                :content current-blog-page-content))))
