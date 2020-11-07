(ns up.chess.app
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [up.chess.css :as css]))

;; create representation of a chess board
(defn create-row [row-num]
  (map (fn [n] (let [letter (char (+ 96 n))
                     coord (str letter row-num)]
                 [(keyword letter) {:coord coord}]))
       (range 1 9)))

(defn create-board []
  (into
   {}
   (for [n (reverse (range 1 9))]
     [(keyword (str n)) (into {} (create-row n))])))

(defn init-db []
  (let [db {:loaded true
            :board (create-board)}] db))

(rf/reg-event-fx
 :initialize   
 (fn [{:keys [db]} _]
   {:db (init-db)
    :css/insert-styles (css/style-list 640 css/scheme2)
    ;;:dispatch [:scratch/load]
    }))

(rf/reg-sub
 :loaded
 (fn [db _]
   (get-in db [:loaded])))

(rf/reg-sub
 :board
 (fn [db _]
   (get-in db [:board])))

(defn is-dark-square? [l n]
  (or (and (= (mod l 2) 1) (= (mod n 2) 1))
      (and (= (mod l 2) 0) (= (mod n 2) 0))))

(defn square-click [evt]
  (let [id (.-id (.-target evt))]
    (js/console.log id)))

(defn board-view []
  (let [board-st (rf/subscribe [:board])]
    (fn []
      (let [board @board-st]
        [:div {:class "chess-board rounded-corner"}
         (for [n (range 1 9)
               l (range 1 9)]
           (let [number (keyword (str n))
                 l1 (char (+ 96 l))
                 letter (keyword l1)
                 square (get (get board number) letter)
                 color (if (is-dark-square? l n) "dark-square" "light-square")
                 id (str l1 n)]
             
             ^{:key id}[:div {:class (str "chess-square " color)
                              :on-click square-click
                              :id id}
                        (:coord square)]))]))))

(defn main-view []
  (let []
    (fn []
      (let []
        [:div {:class "chess-vision"}
         [board-view]]))))

(defn start! []
  (rdom/render [main-view]
                  (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize])
  (start!)
  (println "app.cljs 2020-11-06"))

(defn ^:dev/after-load start []
  (rf/dispatch-sync [:initialize])
  (start!))
