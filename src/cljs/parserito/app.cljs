(ns parserito.app
  (:require [reagent.core :as r]
            [instaparse.core :as insta]
            [cljs.pprint :as pprint]))

(def parse-desc (r/atom {:value ""
                         :parser nil
                         :result nil
                         }))
(def input-text (r/atom nil))

(defn make-parser [text]
  (try
    (let [parser (insta/parser text)]
      [nil parser])
    (catch :default e
      [e nil])))

(defn result-display []
  (if (:result @parse-desc)
    [:div (with-out-str
            (pprint/pprint (:result @parse-desc)))]))

(defn parse-desc-input [{:keys [id]}]
  [:div.float-left.marg
   [:h3 "parse description!"
    [:textarea.textarea-size {:type "text"
                              :value (:value @parse-desc)
                              :on-change (fn [change]

                                           (swap! parse-desc
                                                  assoc
                                                  :value
                                                  (-> change .-target .-value))

                                           (let [[exception parser] (make-parser (:value @parse-desc))]
                                             ((swap! parse-desc
                                                     assoc
                                                     :parser
                                                     parser)

                                              (swap! parse-desc
                                                     assoc
                                                     :result
                                                     (if (and (:parser @parse-desc)
                                                              (seq @input-text))

                                                       ((:parser @parse-desc) @input-text)
                                                       exception)))))}]]])

(defn parse-input []
  [:div.float-left.marg
   [:h3 "parse input!"
    [:textarea.textarea-size {:type "text"
                              :value @input-text
                              :on-change (fn [change]

                                           (reset! input-text
                                                   (-> change .-target .-value))

                                           (swap! parse-desc
                                                  assoc
                                                  :result
                                                  (if (and (:parser @parse-desc)
                                                           (seq @input-text))

                                                    ((:parser @parse-desc) @input-text)

                                                    nil)))}]]])

;; haha css
(defn clearfix [] [:div.clearfix])

(defn calling-component []
  [:div
   [parse-desc-input]
   [parse-input]
   [result-display]
   [clearfix]])

(defn init []
  (r/render-component [calling-component]
                      (.getElementById js/document "container")))
