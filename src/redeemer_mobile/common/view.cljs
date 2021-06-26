(ns redeemer-mobile.common.view
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [redeemer-mobile.common.styles :as style]))

(def ReactNative (js/require "react-native"))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def input (r/adapt-react-class (.-TextInput ReactNative)))
(def button (r/adapt-react-class (.-Button ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def scrollview (r/adapt-react-class (.-ScrollView ReactNative)))
(def logo-img (js/require "./images/logo.png"))
(def menu-img (js/require "./images/hamburger-icon.png"))
(def menu-width 310)

;; -- Pages ------------------------------------------------------------

(defn page-style []
  {:font-size 30 :font-weight "100" :text-align "center"})

(defn home-page []
  [scrollview {:key "home-page"}
   [text {:style (page-style)} "Home Page"]])

(defn about-page []
  [scrollview {:key "about-page" :shows-verticalScrollIndicator false}
   [view {:style {:padding-bottom 60}}
    [text {:style {:font-size 30 :font-weight "bold" :color "#004247"}} "Service Times"]
    [text {:style {:font-size 18}} "•Sunday Morning Ministry Classes (ages 3-adult)"]
    [text {:style {:font-size 18}} "•Sundays at 9:00 AM"]
    [text {:style {:font-size 18}} "•Coffee Fellowship 10-10:30 AM"]
    [text {:style {:font-size 18}} "•Morning Worship Service 11:00 AM"]
    [text {:style {:font-size 18}} "•Nursery is Available (ages NB-2 years)\n"]
    [text {:style {:font-size 30 :font-weight "bold" :color "#004247"}} "What should I Expect"]
    [text {:style {:font-size 18}} "You will be welcomed by loving and caring people. A greeter will show you our coffee area. We do offer child care for age 3 and under, and the greeter can direct you to that as well. The service itself will be worshipful, practical, and gospel-centered. After the service, we normally spend time catching up with one another.\n"]
    [text {:style {:font-size 18}} "Kids are a big part of our church family! We have teen ministries, children’s ministries, and nursery for kids ages newborn-2 years."]
    [text {:style {:font-size 18}} "Our music is a combination of familiar gospel hymns as well as easy-to-sing modern tunes. You will probably be familiar with many of the songs we sing, but even if you are not, the songs should be easy to pick up.\n"]]])

(defn sermons-page []
  [scrollview {:key "sermons-page"}
   [text {:style (page-style)} "Sermons coming soon"]])

(defn blog-page []
  [scrollview {:key "blog-page"}
   [text {:style (page-style)} "Blog coming soon"]])

(defn counseling-page []
  [scrollview {:key "counseling-page"}
   [text {:style (page-style)} "Counseling coming soon"]])

(defn contact-page []
  [scrollview {:key "contact-page"}
   [text {:style (page-style)} "Contact Us"]
   [input {:style style/input-style :placeholder "Name"}]
   [input {:style style/input-style :placeholder "Email Address"}]
   [input {:style style/input-style :placeholder "Comment or message"}]
   [button {:title "Submit" :on-press #(print "pushed")}]])


(def pages
  {:Home       home-page
   :About      about-page
   :Sermons    sermons-page
   :Blog       blog-page
   :Counseling counseling-page
   :Contact    contact-page})

(defn get-page [page-name]
  (or (page-name pages) home-page))

;;End Pages --------------------------------------------

(defn menu-item [icon menu-text]
  [view {:style {:height 45}
         :key (keyword menu-text)}
   [touchable-highlight {:on-press       #(re-frame.core/dispatch [:option-pressed menu-text])
                         :style          {:width menu-width :height 45}
                         :underlay-color "white"}
    [view
     [image {:source icon
             :style  {:height 45 :width 40}}]
     [text {:style {:font-size 30 :flex 1 :position "absolute" :left 60}}
      menu-text]]]])

(defn menu-items []
  [view
   (for [page pages]
     [menu-item logo-img (name (first page))])])

(defn menu-open []
  [view {:style {:width menu-width :height 900 :background-color "white"}}
   [touchable-highlight {:on-press       #(re-frame.core/dispatch [:menu-closed])
                         :style          {:height 50 :width 50 :left (- menu-width 50)}
                         :underlay-color "white"}
    [image {:source menu-img
            :style  {:height 25 :width 35 :position "absolute" :resizeMode "contain" :flex 1 :left "0%" :top 8}}]]
   [menu-items]])

(defn menu-closed []
  [view
   [touchable-highlight {:on-press       #(re-frame.core/dispatch [:menu-opened])
                         :style          {:height 50 :width 50}
                         :underlay-color "white"}
    [image {:source menu-img
            :style  {:height 25 :width 35 :margin 8 :position "absolute" :resizeMode "contain" :flex 1 :left "0%" :top 0}}]]])

(defn menu [state]
  (if (= :open state)
    [menu-open]
    [menu-closed]))

(defn header [state]
  [view {:style {:height 60}}
   [menu state]
   [image {:source logo-img
           :style  {:width 40 :height 60 :margin-top -5 :margin-right 5 :position "absolute" :resizeMode "contain" :flex 1 :right "0%" :top "0%"}}]])

(defn body [menu-state & content]
  (let [height (if (= :open menu-state) 0 "auto")]
    [view {:key   "body"
           :style {:width "80%" :height height :margin-left "auto" :margin-right "auto" :z-index -10 :elevation -10}}
     content]))