(ns parserito.app
  (:require [reagent.core :as r]
            [instaparse.core :as insta]
            [cljs.pprint :as pprint]))

(def state (r/atom {:value ""
                    :parser nil
                    :result nil
                    :input-text nil}))

(defn make-parser [text]
  (try
    (let [parser (insta/parser text)]
      [nil parser])
    (catch :default e
      [e nil])))

(defn result-display []
  (if (:result @state)
    [:div (with-out-str
            (pprint/pprint (:result @state)))]))

(defn try-parse! [failure-value]
  (swap! state
         assoc
         :result
         (if (and (:parser @state)
                  (seq (:input-text @state)))

           ((:parser @state) (:input-text @state))
           failure-value)))

(defn update-parse-desc! [change]
  ;; update parse desc to the value of the element
  (swap! state
         assoc
         :value
         (-> change .-target .-value))

  ;; create a parser from the value of the parse description
  (let [[exception parser] (make-parser (:value @state))]

    ;; set the value of the parser field to the parser
    (swap! state
           assoc
           :parser
           parser)

    (try-parse! exception)))

(defn parse! [change]
  (swap! state
         assoc
         :input-text
         (-> change
             .-target
             .-value))

  (try-parse! nil))

(defn grammar-input [{:keys [id]}]
  [:div.float-left.marg
   [:h3 "parse description!"
    [:textarea.textarea-size {:type "text"
                              :value (:value @state)
                              :on-change update-parse-desc!}]]])

(defn text-corpus []
  [:div.float-left.marg
   [:h3 "parse input!"
    [:textarea.textarea-size {:type "text"
                              :value (:input-text @state)
                              :on-change parse!}]]])

;; haha css
(defn clearfix [] [:div.clearfix])

(defn calling-component []
  [:div
   [grammar-input]
   [text-corpus]
   [result-display]
   [clearfix]])

(defn init []
  (r/render-component [calling-component]
                      (.getElementById js/document "container"))
  (swap! state
         assoc
         :input-text
         "{
 \"numbers\":123,
 \"nulls\":null,
 \"strings\":\"ok\",
 \"stringed numbers\":\"1234\",
 \"bool1\":true,
 \"bool2\":false,
 \"map key\":{\"hi\":\"there\"},
 \"array key\":[1,2,3]
}")

  (swap! state
         assoc
         :value
         "(* A primitive json parser to mess around with. Try changed the parser and the input to see what happens! *)

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

  (let [[exception parser] (make-parser (:value @state))]
    ;; set the value of the parser field to the parser
    (swap! state
           assoc
           :parser
           parser)

    (try-parse! exception)))
