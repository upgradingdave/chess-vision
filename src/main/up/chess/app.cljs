(ns up.chess.app
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [up.chess.css :as css]))

;; App Logic ------------------------------------------------------------------

(defn to-letter [number]
  (char (+ 96 number)))

(defn next-coord []
  (let [l (+ (rand-int 8) 1)
        n (+ (rand-int 8) 1)]

    (str (to-letter l) n)))

;; create representation of a chess board
(defn create-row [row-num]
  (map (fn [n] (let [letter (to-letter n)
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
            :board (create-board)
            :preferences {:side :white
                          :show-coords? false}
            :coord (next-coord)}]
    db))

(defn is-dark-square? [l n]
  (or (and (= (mod l 2) 1) (= (mod n 2) 1))
      (and (= (mod l 2) 0) (= (mod n 2) 0))))

;; ReFrame Events and Subs ----------------------------------------------------

(rf/reg-event-fx
 :initialize   
 (fn [{:keys [db]} _]
   {:db (init-db)
    :css/insert-styles (css/style-list 640 css/scheme2)
    ;;:dispatch [:scratch/load]
    }))

(rf/reg-event-fx
 :guess
 (fn [{:keys [db]} [_ guess-coord]]
   (if (= guess-coord (:coord db))
     {:db (assoc-in db [:coord] (next-coord))}
     {:db db})))

(rf/reg-event-fx
 :set-side-preference
 (fn [{:keys [db]} [_ side]]
   {:db (assoc-in db [:preferences :side] (keyword side))}))

(rf/reg-event-fx
 :set-show-coords-preference
 (fn [{:keys [db]} [_ checked]]
   {:db (assoc-in db [:preferences :show-coords?] checked)}))

(rf/reg-sub
 :loaded
 (fn [db _]
   (get-in db [:loaded])))

(rf/reg-sub
 :board
 (fn [db _]
   (get-in db [:board])))

(rf/reg-sub
 :coord
 (fn [db _]
   (get-in db [:coord])))

(rf/reg-sub
 :preferences
 (fn [db _]
   (get-in db [:preferences])))

;; User Interface -------------------------------------------------------------

(defn square-click [evt]
  (let [id (.-id (.-target evt))]
    (rf/dispatch-sync [:guess id])))

(defn board-view []
  (let [prefs-st (rf/subscribe [:preferences])
        board-st (rf/subscribe [:board])]
    (fn []
      (let [board @board-st
            prefs @prefs-st]
        [:div {:class "chess-board rounded-corner"}
         (for [n (if (= (:side prefs) :white)
                   (reverse (range 1 9))
                   (range 1 9))
               l (range 1 9)]
           (let [number (keyword (str n))
                 l1 (char (to-letter l))
                 letter (keyword l1)
                 square (get (get board number) letter)
                 color (if (is-dark-square? l n) "dark-square" "light-square")
                 id (str l1 n)]
             
             ^{:key id}[:div {:class (str "chess-square " color)
                              :on-click square-click
                              :id id}
                        (when (:show-coords? prefs)
                          (:coord square))]))]))))

(defn prefs-side-click [evt]
  (rf/dispatch [:set-side-preference (.-id (.-target evt))]))

(defn prefs-show-coords-click [evt]
  (let [v (.-checked (.-target evt))]
    (rf/dispatch [:set-show-coords-preference v])))

(defn preferences-view []
  (let [prefs-st (rf/subscribe [:preferences])]
    (fn []
      (let [prefs @prefs-st]
        [:div {:class "preferences"}
         [:div {:class "side-preference"}
          [:input {:type :radio :on-change prefs-side-click
                   :id :white :name :white :value :white
                   :checked (= (:side prefs) :white)}]
          [:label {:for :white} "White"]
          [:input {:type :radio :on-change prefs-side-click
                   :id :black :name :black :value :black
                   :checked (= (:side prefs) :black)}]
          [:label {:for :black} "Black"]]
         [:div {:class "show-coords-preference"}
          [:input {:type :checkbox
                   :id :show-coords :name :show-coords
                   :checked (:show-coords? prefs)
                   :on-change prefs-show-coords-click}]
          [:label {:for :show-coords} "Show Coordinates?"]]
          ]))))

(defn main-view []
  (let [next-coord-st (rf/subscribe [:coord])]
    (fn []
      (let []
        [:div {:class "chess-vision"}
         [:div "Click on the square: " @next-coord-st]
         [preferences-view]
         [board-view]]))))

(defn render! []
  (rdom/render [main-view]
                  (js/document.getElementById "root")))

(defn init []
  (rf/dispatch-sync [:initialize])
  (render!)
  (println "Initialize: app.cljs 2020-11-06"))

(defn ^:dev/after-load start []
  (rf/dispatch-sync [:initialize])
  (render!)
  (println "Reload: app.cljs 2020-11-06"))
