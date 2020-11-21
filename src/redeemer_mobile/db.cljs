(ns redeemer-mobile.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::page keyword?)
(s/def ::menu-state keyword?)
(s/def ::app-db
  (s/keys :req-un [::page]))

;; initial state of app-db
(def app-db {:page       :Home
             :menu-state :closed
             :pages      {:Home       {:navigation "Home" :content "Welcome to Redeemer"}
                          :Learn      {:navigation "Learn" :content "Learn.  Coming soon" :request-url "https://redeemernorwalk.org/learn/"}
                          :Sermons    {:navigation "Sermons" :content "Loading..." :request-url "https://redeemernorwalk.org/sermons/"}
                          :Blog       {:navigation "Blog" :content ["Loading..."] :request-url "http://54.173.4.142/sites/12/posts/?page_num=1" :requested false :current-page-number 1
                                       :pages      {:1 {:content ["Loading..."] :request-url "http://54.173.4.142/sites/12/posts/?page_num=1" :requested false}}}
                          :Counseling {:navigation "Counseling" :content "Counseling page in progress" :request-url "http://54.173.4.142/sites/12/posts/&page_num=1"}
                          :Contact    {:navigation "Contact" :content "Contact us"}}})


