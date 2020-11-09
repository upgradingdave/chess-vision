(ns up.chess.app
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   ["react-transition-group" :refer [CSSTransition]]
   [re-frame.core :as rf]
   [up.chess.css :as css]))

;; App Logic ------------------------------------------------------------------

(defn to-letter [number]
  (char (+ 64 number)))

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
     {:db (-> db
              (assoc-in [:coord] (next-coord))
              (assoc-in [:good-guess] guess-coord))}
     {:db (assoc-in db [:bad-guess] guess-coord)})))

(rf/reg-event-fx
 :clear-guess
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:good-guess] nil)
            (assoc-in [:bad-guess] nil))}))

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

(rf/reg-sub
 :bad-guess
 (fn [db _]
   (get-in db [:bad-guess])))

(rf/reg-sub
 :good-guess
 (fn [db _]
   (get-in db [:good-guess])))

;; User Interface -------------------------------------------------------------

(defn calc-id [l n]
  (str (to-letter l) n))

(defn square-view [prefs board l n]
  (let [bad-guess-st (rf/subscribe [:bad-guess])
        good-guess-st (rf/subscribe [:good-guess])]
    (fn [prefs board l n]
      (let [color (if (is-dark-square? l n) "square--dark" "square--light")
            number-kw (keyword (str n))
            letter (char (to-letter l))
            letter-kw (keyword letter)
            square (get (get board number-kw) letter-kw)
            id (calc-id l n)
            bad-guess @bad-guess-st
            good-guess @good-guess-st]

        [:div {:id id
               :class "square"
               :on-click (fn [_] (rf/dispatch-sync [:guess id]))}
           
         [:div {:class (str "square--normal " color)}
          (when (:show-coords? prefs)
            [:h1 (:coord square)])]

         [:> CSSTransition
          {:classNames "square--good"
           :timeout 2000
           :in (= good-guess id)
           :enter true
           :on-entered #(rf/dispatch [:clear-guess])}
          [:div {:class (str "square--hidden " color)}
           [:i {:class "fas fa-check fa-2x"}]]]

         [:> CSSTransition
          {:classNames "square--bad"
           :timeout 2000
           :in (= bad-guess id)
           :enter true
           :on-entered #(rf/dispatch [:clear-guess])}
          [:div {:class (str "square--hidden " color)}
           [:h1 (:coord square)]
           ;;[:i {:class "fas fa-times fa-2x"}]
           ]]

           ]))))

(defn board-view []
  (let [prefs-st (rf/subscribe [:preferences])
        board-st (rf/subscribe [:board])]
    (fn []
      (let [prefs @prefs-st
            board @board-st]
        [:div {:class "chess-board"}

         (for [n (if (= (:side prefs) :white)
                   (reverse (range 1 9))
                   (range 1 9))
               l (if (= (:side prefs) :black)
                   (reverse (range 1 9))
                   (range 1 9))]
           ^{:key (str l n)} [square-view prefs board l n])]))))

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
         [:div {:class "sidebar__item side-preference"}
          [:input {:type :radio :on-change prefs-side-click
                   :id :white :name :white :value :white
                   :checked (= (:side prefs) :white)}]
          [:label {:for :white} "White"]
          [:input {:type :radio :on-change prefs-side-click
                   :id :black :name :black :value :black
                   :checked (= (:side prefs) :black)}]
          [:label {:for :black} "Black"]]
         [:div {:class "sidebar__item show-coords-preference"}
          [:input {:type :checkbox
                   :id :show-coords :name :show-coords
                   :checked (:show-coords? prefs)
                   :on-change prefs-show-coords-click}]
          [:label {:for :show-coords} "Show Coordinates?"]]]))))

(defn main-view []
  (let [next-coord-st (rf/subscribe [:coord])]
    (fn []
      (let []
        [:div {:class "chess-vision page"}
         [:div {:class "page__sidebar"}
          [preferences-view]
          [:div {:class "sidebar__item"}
           [:h2 "Find Square: " @next-coord-st]]]
         [:div {:class "page__content"}
          [board-view]]
         
         ]))))

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
