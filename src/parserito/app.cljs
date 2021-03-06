(ns parserito.app
  (:require [cljs.pprint :as pprint]
            [clojure.core.async :refer [<! go timeout]]
            [instaparse.core :as insta]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(def initial-parse-description
  "(*\nA primitive json parser to mess around with.\nTry changing the parser and the input to see what happens!\n*)

JSON = object
<keypairs> = (keypair <comma>*)+
keypair = key <colon> value
<key> = string
<value> = string | number | bool | null | object | array
string = <quote> #'[\\w\\d\\s]*' <quote>
number = digit+
<digit> = #'[0-9]'
bool = 'true' | 'false'
null = 'null'
object = <open-curly> keypairs <close-curly>
array = <open-square> (value (<comma> value)*)* <close-square>
<comma> = whitespace ',' whitespace
whitespace =  #'\\s'*
colon = whitespace ':' whitespace
quote = '\"'
open-curly = whitespace '{' whitespace
close-curly = whitespace '}' whitespace
open-square = whitespace '[' whitespace
close-square = whitespace ']' whitespace")

(def initial-input-text "{
  \"numbers\":123,
  \"nulls\":null,
  \"strings\":\"ok\",
  \"stringed numbers\":\"1234\",
  \"bool1\":true,
  \"bool2\":false,
  \"map key\":{\"hi\":\"there\"},
  \"array key\":[1,2,3]
}")

(defn make-parser [text]
  (try
    (let [parser (insta/parser text)]
      [nil parser])
    (catch :default e
      [e nil])))

(defn start-update-latch [n]
  (go (while true
        (<! (timeout n))
        (rf/dispatch [:reset-parser-update-latch nil]))))

;;
;; re-frame
;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; utils

(defn format-json [s]
  (.stringify js/JSON (clj->js s) nil 2))

(defn format-edn [s]
  (with-out-str (pprint/pprint s)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; handlers

(defn initialize-db [db _]
  (let [[exception parser] (make-parser initial-parse-description)]
    {:db {:parser parser
          :exception exception
          :parse-description initial-parse-description
          :input-text initial-input-text
          :parse-result (parser initial-input-text)
          :should-recompute-parser false
          :output-format :edn
          :format-fn format-edn}}))

(defn recompute-parser [db]
  (if (:should-recompute-parser db)
    (let [[exception parser] (make-parser (:parse-description db))]
      (assoc db
             :parser parser
             :exception exception))
    db))

(defn update-parse-description [{:keys [db] :as coeffects} [_ change]]
  (let [db (recompute-parser db)
        parser (:parser db)
        exception (:exception db)]
    {:db (assoc db
                :parse-description change
                :parse-result (if (and parser (seq (:input-text db)))
                                (parser (:input-text db))
                                exception)
                :should-recompute-parser false)}))

(defn update-input-text [{:keys [db] :as coeffects} [_ change]]
  (let [parser (:parser db)
        exception (:exception db)]
    {:db (assoc db
                :input-text change
                :parse-result (if (and parser (seq change))
                                (parser change)
                                exception))}))

(defn reset-parser-update-latch [{:keys [db] :as coeffects} [_ _]]
  {:db (assoc db :should-recompute-parser true)})

(defn toggle-output-format [{:keys [db] :as coeffects} [_ change]]
  (let [output-format (if (= :edn (:output-format db))
                        :json
                        :edn)
        format-fn (case output-format
                    :json format-json
                    :edn format-edn)]

    {:db (assoc db
                :output-format output-format
                :format-fn format-fn)}))

;; event registrations


(rf/reg-event-fx
 :initialize-db
 initialize-db)

(rf/reg-event-fx
 :update-parse-description
 update-parse-description)

(rf/reg-event-fx
 :update-input-text
 update-input-text)

(rf/reg-event-fx
 :reset-parser-update-latch
 reset-parser-update-latch)

(rf/reg-event-fx
 :toggle-output-format
 toggle-output-format)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; queries

(defn parse-description [db v]
  (:parse-description db))

(defn parse-result [db v]
  (:parse-result db))

(defn input-text [db v]
  (:input-text db))

(defn output-format [db v]
  (:output-format db))

(defn format-fn [db v]
  (:format-fn db))

;; query registrations

(rf/reg-sub
 :parse-description
 parse-description)

(rf/reg-sub
 :parse-result
 parse-result)

(rf/reg-sub
 :input-text
 input-text)

(rf/reg-sub
 :format-fn
 format-fn)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;
;; views
;;


(defn grammar-input []
  [:div
   [:h3 "grammar"]
   [:textarea {:type "text"
               :class "form-control"
               :rows "15"
               :value @(rf/subscribe [:parse-description])
               :on-change #(rf/dispatch [:update-parse-description (-> % .-target .-value)])}]])

(defn text-corpus []
  [:div
   [:h3 "input"]
   [:textarea {:type "text"
               :class "form-control"
               :rows "15"
               :value @(rf/subscribe [:input-text])
               :on-change #(rf/dispatch [:update-input-text (-> % .-target .-value)])}]])

(defn output-format-button []
  [:button {:type "button"
            :class "btn-light"
            :value "Switch Formatter"
            :on-click #(rf/dispatch [:toggle-output-format (-> % .-target .-value)])}
   "Switch Formatter"])

(defn result-display []
  (when-let [parse-result @(rf/subscribe [:parse-result])]
    (let [format-fn @(rf/subscribe [:format-fn])]
      [:div
       [:textarea {:type "text"
                   :class "form-control"
                   :rows "30"
                   :value (format-fn parse-result)
                   :readOnly "true"}]])))

(defn left-col []
  [:div {:class "col-lg"}
   [grammar-input]
   [text-corpus]])

(defn right-col []
  [:div {:class "col-lg"}
   [:h3 "result"]
   [output-format-button]
   [result-display]])

(defn calling-component []
  [:div {:class "row"}
   [left-col]
   [right-col]])

(defn init []
  (r/render-component [calling-component]
                      (.getElementById js/document "container"))
  (rf/dispatch-sync [:initialize-db])
  (start-update-latch 1500))

(init)
