(ns up.chess.css
  (:require [garden.core :as g]
            [garden.stylesheet :refer [at-import]]
            [re-frame.core :as rf]))

(def scheme1
  {:light-square-color "#ff7d47"
   :light-square-text "#000000"
   :dark-square-color "#34515e"
   :dark-square-text "#ffffff"
   :page-bg-color "#8eacbb"})

(def scheme2
  {:light-square-color "#e2f1f8"
   :light-square-text "#000000"
   :dark-square-color "#003c8f"
   :dark-square-text "#ffffff"
   :page-bg-color "#8eacbb"})

(defn style-list [width
                  {:keys [light-square-color
                          light-square-text
                          dark-square-color
                          dark-square-text
                          page-bg-color]}]
  [[:html {:background-color page-bg-color}]

   (let [s (/ width 8)]
     [:.chess-board
      {:margin :auto
       :width (str width "px")     
       :display "grid"
       :grid-template-columns (apply str
                                     (interpose " " (repeat 8 (str s "px"))))
       :grid-template-rows (apply str
                                     (interpose " " (repeat 8 (str s "px"))))}
      ])

   [:.rounded-corner
    {:border-radius "5px"
     :-moz-border-radius "5px"
     :-webkit-border-radius "5px"}]

   ;; SQUARES 

   [:.square {:cursor :pointer
              :margin :auto
              :text-align :center
              :line-height (str (/ width 8) "px")
              :position :relative
              :width "100%"
              :height "100%"
              }]

   [:.square--normal {:position :absolute
                      :width "100%"
                      :height "100%"}]

   [:.square--light {:background-color light-square-color
                     :color light-square-text}]

   [:.square--dark {:position :absolute
                    :background-color dark-square-color
                    :color dark-square-text}]

   [:.square--hidden {:position :absolute
                      :width "100%"
                      :height "100%"
                      :opacity 0
                      }]

   [:.square--good-enter {:opacity 1
                          :background-color :green}]
   [:.square--good-enter-active {:opacity 0
                                 :transition "all 2000ms ease-out"}]

   [:.square--bad-enter {:opacity 1
                         :background-color :red}]
   [:.square--bad-enter-active {:opacity 0
                                :transition "all 2000ms ease-out"}]
   ])

(defn insert-styles [style-list]
  "Inserts Stylesheet into document head"
  (let [app-el-id "app-styles"
        app-styles (.getElementById js/document app-el-id)
        el (.createElement js/document "style")
        node (.createTextNode js/document (g/css (concat style-list)))]

    ;; remove old styles tag if it exists
    (when app-styles
      (.removeChild (.-parentNode app-styles) app-styles))

    ;; add styles back 
    (.setAttribute el "id" app-el-id)
    (.appendChild el node)
    (.appendChild (.-head js/document) el)
    el))

(rf/reg-fx         
 :css/insert-styles
 (fn [list-of-css-rules]
   (insert-styles list-of-css-rules)))


