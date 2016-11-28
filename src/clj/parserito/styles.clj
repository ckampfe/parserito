(ns parserito.styles
  (:require [garden.stylesheet :refer [rule]]
            [garden.def :refer [defrule defstyles]]))

(defstyles screen
  [:body
   {:font-family "Helvetica Neue"
    :font-size "16px"
    :line-height 1.5}]

  [:.float-left {:float "left"}]


  [:.marg {:margin "20px"}]

  [:.textarea-size {:min-height "75px"
                    :width "80%"}]

  [:.clearfix {:clear "both"}])

