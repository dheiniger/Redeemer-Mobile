(ns redeemer-mobile.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.events]
            [redeemer-mobile.subs]
            [redeemer-mobile.common.view :refer [header]]
            [redeemer-mobile.common.util :as util]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))       ;;TODO Remove??
(def scrollview (r/adapt-react-class (.-ScrollView ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))                     ;;TODO remove this

(defn body [menu-state & content]
  (let [height (if (= :open menu-state) 0 "auto")]
    [view {:key   "body"
           :style {:width "80%" :height height :margin-left "auto" :margin-right "auto" :z-index -10 :elevation -10}}
     content]))

;;TODO move these
;; -- Pages ------------------------------------------------------------

(defn page-style []                                         ;;TODO add & args and destructure into k/v.  Assoc into current map.
  {:font-size 30 :font-weight "100" :text-align "center"})

(defn requested-page [id]
  (let [page-content (subscribe [:get-page-content id])]
    [scrollview {:key id}
     [text {:style (page-style)} @page-content]]))

(defn home-page []
  [scrollview {:key "home-page"}
   [text {:style (page-style)} "Home Page"]])

(defn learn-page []
  (requested-page "learn-page"))

(defn sermons-page []
  (requested-page "sermons-page"))

(defn render-blog-post [blog-post]
  [view {:key   (:id blog-post)
         :style {:margin-bottom 15}}
   [text {:style {:font-size 30 :font-weight "bold" :text-align "center"}} (:title blog-post)]
   [text {:style {:text-align "center"}} (:date blog-post)]])

(defn render-page-of-blog-posts [page-number page-size]
  (let [current-page (subscribe [:get-blog-posts-page])
        posts-on-page (:content @current-page)]
    [scrollview {:key                     (str "blog-post-page-" page-number)
                 :content-container-style {:paddingBottom 60}}
     (if-not (string? posts-on-page) (map render-blog-post posts-on-page) ;;TODO this is messy
                                     [text {:style {:font-size 30 :font-weight "bold" :text-align "center"}} posts-on-page])
     [view {:key   "navigation bar"
            :style {:flex-direction "row" :justify-content "space-between"}}
      [touchable-highlight
       {:on-press       #(re-frame.core/dispatch [:blog-back-button-pressed page-size page-number])
        :underlay-color "white"}
       [text {:style {:font-weight "bold" :font-size 20 :color "blue"}} "Back"]]
      [touchable-highlight
       {:on-press       #(re-frame.core/dispatch [:blog-next-button-pressed page-size page-number])
        :underlay-color "white"}
       [text {:style {:font-weight "bold" :font-size 20 :color "blue"}} "Next"]]]]))

(defn blog-page []
  (let [blog-posts-page (subscribe [:get-blog-posts-page])
        page-number (:page-number @blog-posts-page)
        page-size (:page-size @blog-posts-page)]
    (render-page-of-blog-posts page-number page-size)))

(defn counseling-page []
  [scrollview {:key "counseling-page"}
   [text {:style (page-style)} "Counseling"]])

(defn contact-page []
  [scrollview {:key "contact-page"}
   [text {:style (page-style)} "Contact Us"]])

;;TODO how would this work with app db?
(defn pages []
  {:Home       home-page
   :Learn      learn-page
   :Sermons    sermons-page
   :Blog       blog-page
   :Counseling counseling-page
   :Contact    contact-page})

(defn get-page [page-name]
  (or (page-name (pages)) home-page))

;; --------------------------------------------------------------

(defn app-root []
  (let [page (subscribe [:get-page])
        menu-state (subscribe [:get-menu-state])]
    (fn []
      [view {:style {:width "100%" :height "100%"}}
       [header @menu-state]
       [body
        @menu-state
        ((get-page @page))]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "RedeemerMobile" #(r/reactify-component app-root)))
