(ns redeemer-mobile.common.props)

(def site-id 12)
(def ambassador-url "http://54.173.4.142/")

(def props {:app-id   1
            :site-id  12
            :apis     {:ambassador       ambassador-url
                       :ambassador-posts (str ambassador-url "sites/" site-id "/posts/?page_num=")}
            :defaults {:page-size 10}})
